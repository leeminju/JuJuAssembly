package com.example.jujuassembly.domain.user.service;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.user.emailAuth.dto.EmailAuthDto;
import com.example.jujuassembly.domain.user.emailAuth.service.EmailAuthService;
import com.example.jujuassembly.domain.user.dto.LoginRequestDto;
import com.example.jujuassembly.domain.user.dto.SignupRequestDto;
import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.dto.UserModifyRequestDto;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.filter.UserDetailsImpl;
import com.example.jujuassembly.global.jwt.JwtUtil;
import com.example.jujuassembly.global.s3.S3Manager;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
  private final JwtUtil jwtUtil;
  private final S3Manager s3Manager;

  public void signup(SignupRequestDto signupRequestDto) {

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

    // categoryId 검증
    categoryRepository.findCategoryByIdOrElseThrow(firstPreferredCategoryId);
    categoryRepository.findCategoryByIdOrElseThrow(secondPreferredCategoryId);

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
    emailAuthService.setSentCodeByLoginIdAtRedis(loginId, nickname, email,
        passwordEncoder.encode(password), firstPreferredCategoryId, secondPreferredCategoryId,
        sentCode);

  }

  @Transactional
  public UserResponseDto verificateCode(String verificationCode, String loginId) {

    EmailAuthDto emailAuthDto = emailAuthService.checkVerifyVerificationCode(loginId,
        verificationCode);

    String nickname = emailAuthDto.getNickname();
    String email = emailAuthDto.getEmail();
    String password = emailAuthDto.getPassword();
    Long firstPreferredCategoryId = emailAuthDto.getFirstPreferredCategoryId();
    Category firstPreferredCategory = categoryRepository.findCategoryByIdOrElseThrow(
        firstPreferredCategoryId);
    Long secondPreferredCategoryId = emailAuthDto.getSecondPreferredCategoryId();
    Category secondPreferredCategory = categoryRepository.findCategoryByIdOrElseThrow(
        secondPreferredCategoryId);

    User user = new User(loginId, nickname, email, password, firstPreferredCategory,
        secondPreferredCategory);

    userRepository.save(user);

    //인증 완료되면 redis의 임시 데이터 삭제
    emailAuthService.concludeEmailAuthentication(loginId);

    return new UserResponseDto(user);
  }

  public String login(LoginRequestDto requestDto) {
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
    if (jwtUtil.checkIsLoggedIn(loginId)) {
      return jwtUtil.getAccessTokenByLoginId(loginId);
    }

    // 회원탈퇴여부 확인
    if (user.getIsArchived()) {
      throw new ApiException("이미 회원탈퇴한 유저입니다.", HttpStatus.BAD_REQUEST);
    }

    // access token 및 refresh token
    String accessToken = jwtUtil.createAccessToken(loginId);
    jwtUtil.saveAccessTokenByLoginId(loginId, accessToken);

    String refreshToken = jwtUtil.createRefreshToken(loginId);
    jwtUtil.saveRefreshTokenByAccessToken(accessToken, refreshToken);

    return accessToken;
  }

  public UserDetailResponseDto viewMyProfile(User user) {
    return new UserDetailResponseDto(user);
  }

  //프로필 수정
  @Transactional
  public UserDetailResponseDto modifyProfile(Long userId, User user,
      UserModifyRequestDto modifyRequestDto) {

    if (!user.getId().equals(userId)) {
      throw new ApiException("본인만 변경할 수 있습니다.", HttpStatus.UNAUTHORIZED);
    }

    User loginUser = userRepository.findUserByIdOrElseThrow(userId);
    Category category1 = categoryRepository.findCategoryByIdOrElseThrow(
        modifyRequestDto.getFirstPreferredCategoryId());
    Category category2 = categoryRepository.findCategoryByIdOrElseThrow(
        modifyRequestDto.getSecondPreferredCategoryId());

    if (user.getKakaoId() == null) {

      if (!passwordEncoder.matches(modifyRequestDto.getCurrentPassword(), user.getPassword())) {
        throw new ApiException("현재 비밀번호가 일치 하지 않습니다!", HttpStatus.BAD_REQUEST);
      }

      if (!modifyRequestDto.getPassword().equals(modifyRequestDto.getPasswordCheck())) {
        throw new ApiException("비밀번호 확인이 일치 하지 않습니다!", HttpStatus.BAD_REQUEST);
      }

      //현재와 동일한 닉네임으로 변경 가능
      if (!user.getNickname().equals(modifyRequestDto.getNickname())) {
        if (userRepository.findByNickname(modifyRequestDto.getNickname()).isPresent()) {
          throw new ApiException("중복된 nickname 입니다.", HttpStatus.BAD_REQUEST);
        }
      }
      String encodePassword = passwordEncoder.encode(modifyRequestDto.getPassword());
      loginUser.updateUser(modifyRequestDto, encodePassword, category1, category2);
    } else {
      loginUser.updateKakaoUser(modifyRequestDto, category1, category2);
    }

    return new UserDetailResponseDto(loginUser);
  }

  //프로필 사진 추가
  @Transactional
  public UserDetailResponseDto uploadImage(Long userId, MultipartFile image) throws Exception {
    User user = userRepository.findUserByIdOrElseThrow(userId);
    s3Manager.deleteAllImageFiles(userId.toString(), S3Manager.USER_DIRECTORY_NAME);

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.", HttpStatus.BAD_REQUEST);
      }
      String url = s3Manager.upload(image, S3Manager.USER_DIRECTORY_NAME, userId);
      user.updateUserImage(url);
    }

    return new UserDetailResponseDto(user);
  }

  // 회원 탈퇴
  @Transactional
  public void deleteAccount(Long userId, String password, UserDetailsImpl userDetails) {
    User user = userRepository.findUserByIdOrElseThrow(userId);
    if (!userId.equals(userDetails.getUser().getId())) {
      throw new ApiException("해당 사용자만 로그아웃 할 수 있습니다.", HttpStatus.UNAUTHORIZED);
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new ApiException("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
    }

    user.setIsArchived(true);
  }

  public void logout(String loginId, HttpServletResponse response) throws IOException {
    String accessToken = jwtUtil.getAccessTokenByLoginId(loginId);
    jwtUtil.removeTokensAtRedisDB(accessToken, response);
  }
}
