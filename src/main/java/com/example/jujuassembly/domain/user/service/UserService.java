package com.example.jujuassembly.domain.user.service;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.emailAuth.entity.EmailAuth;
import com.example.jujuassembly.domain.emailAuth.repository.EmailAuthRepository;
import com.example.jujuassembly.domain.emailAuth.service.EmailAuthService;
import com.example.jujuassembly.domain.user.dto.LoginRequestDto;
import com.example.jujuassembly.domain.user.dto.SignupRequestDto;
import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.dto.UserModifyRequestDto;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.jwt.JwtUtil;
import com.example.jujuassembly.global.s3.S3Manager;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailAuthService emailAuthService;
  private final CategoryRepository categoryRepository;
  private final EmailAuthRepository emailAuthRepository;
  private final JwtUtil jwtUtil;
  private final S3Manager s3Manager;

  public String signup(SignupRequestDto signupRequestDto) {

    String loginId = signupRequestDto.getLoginId();
    String nickname = signupRequestDto.getNickname();
    String email = signupRequestDto.getEmail();

    String password = signupRequestDto.getPassword();
    String passwordCheck = signupRequestDto.getPasswordCheck();

    Long firstPreferredCategoryId = signupRequestDto.getFirstPreferredCategoryId();
    Long secondPreferredCategoryId = signupRequestDto.getSecondPreferredCategoryId();

    // id, nickname, email 중복 검증
    if (userRepository.findByLoginId(loginId).isPresent()) {
      throw new ApiException("중복된 loginId 입니다.", HttpStatus.BAD_REQUEST);
    }
    if (userRepository.findByNickname(nickname).isPresent()) {
      throw new ApiException("중복된 nickname 입니다.", HttpStatus.BAD_REQUEST);
    }
    if (userRepository.findByEmail(email).isPresent()) {
      throw new ApiException("중복된 email 입니다.", HttpStatus.BAD_REQUEST);
    }

    // 회원가입을 하고있는(인증번호 확인중인 상태) id, nickname, email 중복 검증
    if (emailAuthRepository.findByLoginId(loginId).isPresent()) {
      throw new ApiException("현재 회원가입 중인 loginId 입니다.", HttpStatus.BAD_REQUEST);
    }
    if (emailAuthRepository.findByNickname(nickname).isPresent()) {
      throw new ApiException("현재 회원가입 중인 nickname 입니다.", HttpStatus.BAD_REQUEST);
    }
    if (emailAuthRepository.findByEmail(email).isPresent()) {
      throw new ApiException("현재 회원가입중인 email 입니다.", HttpStatus.BAD_REQUEST);
    }

    // categoryId 검증
    categoryRepository.getById(firstPreferredCategoryId);
    categoryRepository.getById(secondPreferredCategoryId);

    // password 확인
    // 1. nickname과 같은 값이 포함됐는지
    if (password.contains(nickname)) {
      throw new ApiException("비밀번호에 닉네임과 같은 값이 포함될 수 없습니다.", HttpStatus.BAD_REQUEST);
    }
    // 2. 비밀번호 확인이 비밀번호와 일치하는지
    if (!password.equals(passwordCheck)) {
      throw new ApiException("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
    }

    // 인증번호 메일 보내기
    String sentCode = emailAuthService.sendVerificationCode(email);

    // redis에 저장하여 5분 내로 인증하도록 설정
    emailAuthService.setSentCodeByLoginIdAtRedis(loginId, sentCode);

    // 재입력 방지를 위해 DB에 입력된 데이터를 임시 저장
    emailAuthRepository.save(
        new EmailAuth(loginId, nickname, email, passwordEncoder.encode(password),
            firstPreferredCategoryId, secondPreferredCategoryId, sentCode));

    return loginId;
  }

  public UserResponseDto verificateCode(HttpServletRequest request, HttpServletResponse response,
      String loginId) {
    String verificationCode = request.getHeader(EmailAuthService.VERIFICATION_CODE_HEADER);
    EmailAuth emailAuth = emailAuthService.checkVerifyVerificationCode(loginId, verificationCode);
    String nickname = emailAuth.getNickname();
    String email = emailAuth.getEmail();
    String password = emailAuth.getPassword();
    Long firstPreferredCategoryId = emailAuth.getFirstPreferredCategoryId();
    Category firstPreferredCategory = categoryRepository.getById(firstPreferredCategoryId);
    Long secondPreferredCategoryId = emailAuth.getSecondPreferredCategoryId();
    Category secondPreferredCategory = categoryRepository.getById(secondPreferredCategoryId);

    User user = new User(loginId, nickname, email, password, firstPreferredCategory,
        secondPreferredCategory);

    userRepository.save(user);

    //인증 완료되면 임시 데이터 삭제
    emailAuthService.concludeEmailAuthentication(emailAuth, response);

    return new UserResponseDto(user);
  }

  public UserResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {
    String loginId = requestDto.getLoginId();
    // 아이디 확인
    User user = userRepository.findByLoginId(loginId).orElse(null);
    if (user == null) {
      throw new ApiException("등록된 유저가 없습니다.", HttpStatus.NOT_FOUND);
    }
    // 패스워드 확인
    if (!(passwordEncoder.matches(requestDto.getPassword(), user.getPassword()))) {
      throw new ApiException("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
    }

    // 중복 로그인 확인
    jwtUtil.checkIsLoggedIn(loginId, response);

    // access token 및 refresh token
    String accessToken = jwtUtil.createAccessToken(loginId);
    response.setHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken);
    jwtUtil.saveAccessTokenByLoginId(loginId, accessToken);

    String refreshToken = jwtUtil.createRefreshToken(loginId);
    jwtUtil.saveRefreshTokenByAccessToken(accessToken, refreshToken);

    return new UserResponseDto(user);
  }

  public void logout(HttpServletRequest request, HttpServletResponse response) {
    String accessToken = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);
    if (!jwtUtil.validateToken(accessToken.substring(7))) {
      String responseHeaderAccessToken = response.getHeader(JwtUtil.AUTHORIZATION_HEADER);
      jwtUtil.removeRefreshToken(responseHeaderAccessToken);
      jwtUtil.removeAccessToken(responseHeaderAccessToken);
    } else {
      jwtUtil.removeRefreshToken(accessToken);
      jwtUtil.removeAccessToken(accessToken);
    }

    response.setHeader(JwtUtil.AUTHORIZATION_HEADER, "logged-out");
  }


  public UserDetailResponseDto viewProfile(Long userId, User user) {
    User loginUser = userRepository.getById(userId);
    return new UserDetailResponseDto(user);
  }


  public UserDetailResponseDto viewMyProfile(User user) {
    User loginUser = userRepository.getById(user.getId());
    return new UserDetailResponseDto(user);
  }

  //프로필 수정
  @Transactional
  public UserDetailResponseDto modifyProfile(Long userId, User user,
      UserModifyRequestDto modifyRequestDto) {
    if (!user.getId().equals(userId)) {
      throw new ApiException("본인만 변경할 수 있습니다.", HttpStatus.UNAUTHORIZED);
    }

    User loginUser = userRepository.getById(userId);
    loginUser.updateUser(modifyRequestDto);
    userRepository.save(loginUser);
    return new UserDetailResponseDto(loginUser);
  }

  //프로필 사진 추가
  public UserDetailResponseDto uploadImage(Long userId, MultipartFile image) throws Exception {
    User user = userRepository.getById(userId);
    s3Manager.deleteAllImageFiles(userId.toString(), "users");

    if (image != null && !image.isEmpty()) {
      String url = s3Manager.upload(image, "users", userId);
      user.updateUserImage(url);
      userRepository.save(user);
    }
    return new UserDetailResponseDto(user);
  }

  // 회원 탈퇴
  @Transactional
  public void deleteAccount(Long userId, String password, UserDetailsImpl userDetails) {
    User user = userRepository.getById(userId);
    if (!userId.equals(userDetails.getUser().getId())) {
      throw new ApiException("해당 사용자만 로그아웃 할 수 있습니다.", HttpStatus.UNAUTHORIZED);
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new ApiException("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
    }

    user.setIsArchived(true);
  }

}
