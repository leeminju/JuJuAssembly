package com.example.jujuassembly.domain.emailAuth.service;

import com.example.jujuassembly.domain.emailAuth.entity.EmailAuth;
import com.example.jujuassembly.domain.emailAuth.repository.EmailAuthRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.mail.EmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

  private final EmailService emailService;
  private final EmailAuthRepository emailAuthRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private final PasswordEncoder passwordEncoder;

  public static final String LOGIN_ID_AUTHORIZATION_HEADER = "LoginIdAuth";
  public static final String VERIFICATION_CODE_HEADER = "VerificationCode";


  /**
   * 사용자가 회원가입을 위해 인증번호 받을 때 사용되는 메서드
   **/
  public void checkAndSendVerificationCode(String loginId, String nickname, String email,
      String password, Long firstPreferredCategoryId, Long secondPreferredCategoryId,
      HttpServletResponse response) {

    // 인증번호 메일 보내기
    String sentCode = sendVerificationCode(email);

    // redis에 저장하여 5분 내로 인증하도록 설정
    redisTemplate.opsForValue().set(loginId, sentCode, 5 * 60 * 1000, TimeUnit.MILLISECONDS);

    // 쿠키에 인증할 loginId을 넣어보냄
    Cookie cookie = getCookieByLoginId(loginId);
    setCookie(cookie, response);

    // 재입력 방지를 위해 DB에 입력된 데이터를 임시 저장
    emailAuthRepository.save(
        new EmailAuth(loginId, nickname, email, passwordEncoder.encode(password),
            firstPreferredCategoryId, secondPreferredCategoryId, sentCode));
  }

  /**
   * 사용자가 인증번호 입력시 사용되는 메서드
   **/
  public EmailAuth checkVerifyVerificationCode(String loginId, String verificationCode) {
    // 가장 최근에 만들어진 인증 데이터 조회 (5분 이내 인증에 실패했을 경우 중복 생성 될 수 있음)
    var emailAuth = emailAuthRepository.findTopByLoginIdOrderByCreatedAtDesc(loginId)
        .orElseThrow(()
            -> new IllegalArgumentException("인증 가능한 loginId가 아닙니다."));

    // 5분이 지났는지 검증
    if (!redisTemplate.hasKey(loginId)) {
      throw new ApiException("5분 초과, 다시 인증하세요", HttpStatus.REQUEST_TIMEOUT);
    }

    // 인증번호 일치하는지 확인
    if (!emailAuth.getSentCode().equals(verificationCode)) {
      throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
    }

    return emailAuth;
  }

  public void endEmailAuth(EmailAuth emailAuth, HttpServletResponse response) {
    redisTemplate.delete(emailAuth.getLoginId());
    emailAuthRepository.delete(emailAuth);
    Cookie cookie = new Cookie(LOGIN_ID_AUTHORIZATION_HEADER, null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  private String sendVerificationCode(String email) {
    String generatedCode = generateRandomCode();

    // 이메일로 인증 번호 발송
    emailService.sendEmail(email, "회원가입을 위한 인증 번호 메일입니다.", "인증번호: " + generatedCode);
    return generatedCode;
  }

  private Cookie getCookieByLoginId(String loginId) {
    Cookie cookie = new Cookie(LOGIN_ID_AUTHORIZATION_HEADER, loginId);
    cookie.setPath("/");
    cookie.setMaxAge(5 * 60);
    return cookie;
  }

  private void setCookie(Cookie cookie, HttpServletResponse response) {
    response.addCookie(cookie);
  }

  private String generateRandomCode() {
    // 랜덤한 6자리 숫자 생성
    Random random = new Random();
    int code = 100000 + random.nextInt(900000);
    return String.valueOf(code);
  }

}

