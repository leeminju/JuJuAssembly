package com.example.jujuassembly.domain.user.service;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.entity.repository.CategoryRepository;
import com.example.jujuassembly.domain.user.dto.SingupRequestDto;
import com.example.jujuassembly.domain.user.dto.SignupResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.domain.user.service.emailAuth.EmailAuth;
import com.example.jujuassembly.domain.user.service.emailAuth.EmailAuthRepository;
import com.example.jujuassembly.domain.user.service.emailAuth.EmailAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailAuthService emailAuthService;
  private final CategoryRepository categoryRepository;
  private final EmailAuthRepository emailAuthRepository;

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
      throw new IllegalArgumentException("중복된 loginId 입니다.");
    }
    if (!emailAuthRepository.findByNickname(nickname).isEmpty()) {
      throw new IllegalArgumentException("중복된 nickname 입니다.");
    }
    if (!emailAuthRepository.findByEmail(email).isEmpty()) {
      throw new IllegalArgumentException("중복된 email 입니다.");
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

    emailAuthService.checkAndSendVerificationCode(loginId, nickname, email,
        passwordEncoder.encode(password),
        firstPreferredCategoryId, secondPreferredCategoryId, response);
  }

  public SignupResponseDto verificateCode(String verificationCode, String nickname,
      HttpServletResponse response) {
    EmailAuth emailAuth = emailAuthService.checkVerifyVerificationCode(nickname, verificationCode);
    String loinId = emailAuth.getLoginId();
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
    User user = new User(loinId, nickname, email, password, null,
        null);

    userRepository.save(user);

    //인증 완료되면 임시 데이터 삭제
    emailAuthService.endEmailAuth(emailAuth, response);

    return new SignupResponseDto(user);
  }
}
