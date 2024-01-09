package com.example.jujuassembly.domain.user.service;

import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.emailAuth.entity.EmailAuth;
import com.example.jujuassembly.domain.emailAuth.repository.EmailAuthRepository;
import com.example.jujuassembly.domain.emailAuth.service.EmailAuthService;
import com.example.jujuassembly.domain.user.dto.LoginRequestDto;
import com.example.jujuassembly.domain.user.dto.SingupRequestDto;
import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.dto.UserModifyRequestDto;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.jwt.JwtUtil;
import com.example.jujuassembly.global.s3.S3Manager;
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

  public void signup(SingupRequestDto singupRequestDto, HttpServletResponse response) {

    String loginId = singupRequestDto.getLoginId();
    String nickname = singupRequestDto.getNickname();
    String email = singupRequestDto.getEmail();

    String password = singupRequestDto.getPassword();
    String passwordCheck = singupRequestDto.getPasswordCheck();

    Long firstPreferredCategoryId = singupRequestDto.getFirstPreferredCategoryId();
    Long secondPreferredCategoryId = singupRequestDto.getSecondPreferredCategoryId();

    // id, nickname, email 중복 검증
    if (!userRepository.findByLoginId(loginId).isEmpty()) {
      throw new IllegalArgumentException("중복된 loginId 입니다.");
    }
    if (!userRepository.findByNickname(nickname).isEmpty()) {
      throw new IllegalArgumentException("중복된 nickname 입니다.");
    }
    if (!userRepository.findByEmail(email).isEmpty()) {
      throw new IllegalArgumentException("중복된 email 입니다.");
    }

    // 회원가입을 하고있는(인증번호 확인중인 상태) id, nickname, email 중복 검증
    if (!emailAuthRepository.findByLoginId(loginId).isEmpty()) {
      throw new IllegalArgumentException("현재 회원가입 중인 loginId 입니다.");
    }
    if (!emailAuthRepository.findByNickname(nickname).isEmpty()) {
      throw new IllegalArgumentException("현재 회원가입 중인 nickname 입니다.");
    }
    if (!emailAuthRepository.findByEmail(email).isEmpty()) {
      throw new IllegalArgumentException("현재 회원가입중인 email 입니다.");
    }

//    // categoryId 검증
//    categoryRepository.findById(firstPreferredCategoryId)
//        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 firstPreferredCategoryId 입니다."));
//    categoryRepository.findById(secondPreferredCategoryId)
//        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 secondPreferredCategoryId 입니다."));

    // password 확인
    // 1. nickname과 같은 값이 포함됐는지
    if (password.contains(nickname)) {
      throw new IllegalArgumentException("비밀번호에 닉네임과 같은 값이 포함될 수 없습니다.");
    }
    // 2. 비밀번호 확인이 비밀번호와 일치하는지
    if (!password.equals(passwordCheck)) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
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
//    Long firstPreferredCategoryId = emailAuth.getFirstPreferredCategoryId();
//    Category firstPreferredCategory = categoryRepository.findById(firstPreferredCategoryId)
//        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 firstPreferredCategoryId입니다."));
//    Long secondPreferredCategoryId = emailAuth.getSecondPreferredCategoryId();
//    Category secondPreferredCategory = categoryRepository.findById(secondPreferredCategoryId)
//        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 secondPreferredCategoryId입니다."));

//    User user = new User(loinId, nickname, email, password, firstPreferredCategory,
//        secondPreferredCategory);

    // 추후 수정
    User user = new User(loginId, nickname, email, password, null,
        null);

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
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    // access token 및 refresh token
    String accessToken = jwtUtil.createAccessToken(loginId);
    response.setHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken);

    String refreshToken = jwtUtil.createRefreshToken(loginId);
    jwtUtil.saveRefreshToken(accessToken.substring(7), refreshToken.substring(7));

    return new UserResponseDto(user);
  }


  public UserDetailResponseDto viewProfile(Long userId, User user) {
    //본인 확인
    if (!user.getId().equals(userId)){
      throw new IllegalArgumentException("본인만 조회할 수 있습니다.");
    }

    User loginUser = userRepository.findById(userId).orElseThrow(()-> new  IllegalArgumentException("존재하지 않는 유저입니다."));
    return new UserDetailResponseDto(user);
  }

  //프로필 수정
  @Transactional
 public UserDetailResponseDto modifyProfile(Long userId, User user, UserModifyRequestDto modifyRequestDto){
   if (!user.getId().equals(userId)){
     throw new IllegalArgumentException("본인만 조회할 수 있습니다.");
   }

   User loginUser = userRepository.findById(userId).orElseThrow(()-> new  IllegalArgumentException("존재하지 않는 유저입니다."));
   loginUser.updateUser(modifyRequestDto);
   userRepository.save(loginUser);
   return new UserDetailResponseDto(loginUser);
 }

  //프로필 사진 추가
  public UserDetailResponseDto uploadImage(Long userId,MultipartFile image) throws Exception{
    User user = userRepository.findById(userId).orElseThrow(()-> new  IllegalArgumentException("존재하지 않는 유저입니다."));
    s3Manager.deleteAllImageFiles(userId.toString(), "users");

    if (image!=null && !image.isEmpty()){
      String url = s3Manager.upload(image,"users",userId);
      user.setImage(url);
      userRepository.save(user);
    }
    return new UserDetailResponseDto(user);
  }


  public void logout(String bearerToken) {

  }

}
