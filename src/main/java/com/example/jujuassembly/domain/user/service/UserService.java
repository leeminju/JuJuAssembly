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

  public void signup(SignupRequestDto signupRequestDto, HttpServletResponse response) {

    String loginId = signupRequestDto.getLoginId();
    String nickname = signupRequestDto.getNickname();
    String email = signupRequestDto.getEmail();

    String password = signupRequestDto.getPassword();
    String passwordCheck = signupRequestDto.getPasswordCheck();

    Long firstPreferredCategoryId = signupRequestDto.getFirstPreferredCategoryId();
    Long secondPreferredCategoryId = signupRequestDto.getSecondPreferredCategoryId();

    // id, nickname, email 중복 검증
    if (!userRepository.findByLoginId(loginId).isEmpty()) {
      throw new ApiException("중복된 loginId 입니다.", HttpStatus.BAD_REQUEST);
    }
    if (!userRepository.findByNickname(nickname).isEmpty()) {
      throw new ApiException("중복된 nickname 입니다.", HttpStatus.BAD_REQUEST);
    }
    if (!userRepository.findByEmail(email).isEmpty()) {
      throw new ApiException("중복된 email 입니다.", HttpStatus.BAD_REQUEST);
    }

    // 회원가입을 하고있는(인증번호 확인중인 상태) id, nickname, email 중복 검증
    if (!emailAuthRepository.findByLoginId(loginId).isEmpty()) {
      throw new ApiException("현재 회원가입 중인 loginId 입니다.", HttpStatus.BAD_REQUEST);
    }
    if (!emailAuthRepository.findByNickname(nickname).isEmpty()) {
      throw new ApiException("현재 회원가입 중인 nickname 입니다.", HttpStatus.BAD_REQUEST);
    }
    if (!emailAuthRepository.findByEmail(email).isEmpty()) {
      throw new ApiException("현재 회원가입중인 email 입니다.", HttpStatus.BAD_REQUEST);
    }

    // categoryId 검증
    categoryRepository.findById(firstPreferredCategoryId)
        .orElseThrow(() -> new ApiException("존재하지 않는 firstPreferredCategoryId 입니다.", HttpStatus.NOT_FOUND));
    categoryRepository.findById(secondPreferredCategoryId)
        .orElseThrow(() -> new ApiException("존재하지 않는 secondPreferredCategoryId 입니다.", HttpStatus.NOT_FOUND));

    // password 확인
    // 1. nickname과 같은 값이 포함됐는지
    if (password.contains(nickname)) {
      throw new ApiException("비밀번호에 닉네임과 같은 값이 포함될 수 없습니다.", HttpStatus.BAD_REQUEST);
    }
    // 2. 비밀번호 확인이 비밀번호와 일치하는지
    if (!password.equals(passwordCheck)) {
      throw new ApiException("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
    }

    emailAuthService.checkAndSendVerificationCode(loginId, nickname, email, password,
        firstPreferredCategoryId, secondPreferredCategoryId, response);
  }

  public UserResponseDto verificateCode(String verificationCode, String loginId,
      HttpServletResponse response) {
    EmailAuth emailAuth = emailAuthService.checkVerifyVerificationCode(loginId, verificationCode);
    String nickname = emailAuth.getNickname();
    String email = emailAuth.getEmail();
    String password = emailAuth.getPassword();
    Long firstPreferredCategoryId = emailAuth.getFirstPreferredCategoryId();
    Category firstPreferredCategory = categoryRepository.findById(firstPreferredCategoryId)
        .orElseThrow(() -> new ApiException("존재하지 않는 firstPreferredCategoryId입니다.", HttpStatus.NOT_FOUND));
    Long secondPreferredCategoryId = emailAuth.getSecondPreferredCategoryId();
    Category secondPreferredCategory = categoryRepository.findById(secondPreferredCategoryId)
        .orElseThrow(() -> new ApiException("존재하지 않는 secondPreferredCategoryId입니다.", HttpStatus.NOT_FOUND));

    User user = new User(loginId, nickname, email, password, firstPreferredCategory,
        secondPreferredCategory);

    userRepository.save(user);

    //인증 완료되면 임시 데이터 삭제
    emailAuthService.endEmailAuth(emailAuth, response);

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

    // access token 및 refresh token
    String accessToken = jwtUtil.createAccessToken(loginId);
    response.setHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken);

    String refreshToken = jwtUtil.createRefreshToken(loginId);
    jwtUtil.saveRefreshToken(loginId, refreshToken);

    return new UserResponseDto(user);
  }

  public void logout(HttpServletRequest request) {
    String loginId = jwtUtil.getLoginIdFromHeader(request);
    String accessTokenValue = jwtUtil.resolveToken(request);

    jwtUtil.removeRefreshToken(accessTokenValue);
    jwtUtil.removeAccessToken(loginId);
  }


  public UserDetailResponseDto viewProfile(Long userId, User user) {
    //본인 확인
    if (!user.getId().equals(userId)){
      throw new ApiException("본인만 조회할 수 있습니다.", HttpStatus.UNAUTHORIZED);
    }

    User loginUser = userRepository.findById(userId).orElseThrow(()-> new  ApiException("존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND));
    return new UserDetailResponseDto(user);
  }

  //프로필 수정
  @Transactional
 public UserDetailResponseDto modifyProfile(Long userId, User user, UserModifyRequestDto modifyRequestDto){
   if (!user.getId().equals(userId)){
     throw new ApiException("본인만 조회할 수 있습니다.", HttpStatus.UNAUTHORIZED);
   }

   User loginUser = userRepository.findById(userId).orElseThrow(()-> new  ApiException("존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND));
   loginUser.updateUser(modifyRequestDto);
   userRepository.save(loginUser);
   return new UserDetailResponseDto(loginUser);
 }

  //프로필 사진 추가
  public UserDetailResponseDto uploadImage(Long userId,MultipartFile image) throws Exception{
    User user = userRepository.findById(userId).orElseThrow(()-> new  ApiException("존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND));
    s3Manager.deleteAllImageFiles(userId.toString(), "users");

    if (image!=null && !image.isEmpty()){
      String url = s3Manager.upload(image,"users",userId);
      user.updateUserImage(url);
      userRepository.save(user);
    }
    return new UserDetailResponseDto(user);
  }

  // 회원 탈퇴
  @Transactional
  public void deleteAccount(Long userId, String password, UserDetailsImpl userDetails) {
    User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("등록된 유저가 없습니다.", HttpStatus.NOT_FOUND));
    if (!userId.equals(userDetails.getUser().getId())) {
      throw new ApiException("해당 사용자만 로그아웃 할 수 있습니다.", HttpStatus.UNAUTHORIZED);
    }

    if (!passwordEncoder.matches(password,user.getPassword())) {
      throw new ApiException("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
    }

    user.setIsArchived(true);
  }
}
