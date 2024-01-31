package com.example.jujuassembly.domain.user.emailAuth.service;

import com.example.jujuassembly.domain.user.emailAuth.dto.EmailAuthDto;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.mail.EmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

  private final EmailService emailService;
  private final RedisTemplate<String, String> redisTemplate;

  public static final String LOGIN_ID_AUTHORIZATION_HEADER = "LoginIdAuth";
  public static final String VERIFICATION_CODE_HEADER = "VerificationCode";


  /**
   * 사용자가 인증번호 입력시 사용되는 메서드
   **/
  public EmailAuthDto checkVerifyVerificationCode(String loginId, String verificationCode) {

    if (Boolean.FALSE.equals(redisTemplate.hasKey(loginId))) {
      throw new ApiException("인증 시간이 만료되었습니다.", HttpStatus.GONE);
    }
    String nickname = (String) redisTemplate.opsForHash().get(loginId, "nickname");
    String email = (String) redisTemplate.opsForHash().get(loginId, "email");
    String password = (String) redisTemplate.opsForHash().get(loginId, "password");
    Long firstPreferredCategoryId = Long.parseLong(
        (String) redisTemplate.opsForHash().get(loginId, "firstPreferredCategoryId"));
    Long secondPreferredCategoryId = Long.parseLong(
        (String) redisTemplate.opsForHash().get(loginId, "secondPreferredCategoryId"));
    String sentCode = (String) redisTemplate.opsForHash().get(loginId, "sentCode");

    // 인증번호 일치하는지 확인
    if (!sentCode.equals(verificationCode)) {
      throw new ApiException("인증번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
    }

    return new EmailAuthDto(loginId, nickname, email, password, firstPreferredCategoryId,
        secondPreferredCategoryId);
  }

  public void concludeEmailAuthentication(String loginId) {
    // 인증 정보 삭제
    redisTemplate.delete(loginId);
  }

  @Async
  public void setSentCodeByLoginIdAtRedis(String loginId, String nickname, String email,
      String password, Long firstPreferredCategoryId, Long secondPreferredCategoryId,
      String sentCode) {
    redisTemplate.opsForHash().put(loginId, "nickname", nickname);
    redisTemplate.opsForHash().put(loginId, "email", email);
    redisTemplate.opsForHash().put(loginId, "password", password);
    redisTemplate.opsForHash()
        .put(loginId, "firstPreferredCategoryId", firstPreferredCategoryId.toString());
    redisTemplate.opsForHash()
        .put(loginId, "secondPreferredCategoryId", secondPreferredCategoryId.toString());
    redisTemplate.opsForHash().put(loginId, "sentCode", sentCode);
    redisTemplate.expire(loginId, 5 * 60, TimeUnit.SECONDS);
  }

  public String sendVerificationCode(String email) {
    String generatedCode = generateRandomCode();

    // 이메일로 인증 번호 발송
    emailService.sendEmail(email, "주주총회 회원가입을 위한 인증 코드 메일입니다.", "인증번호: " + generatedCode);
    return generatedCode;
  }

  public Cookie getCookieByLoginId(String loginId) {
    Cookie cookie = new Cookie(LOGIN_ID_AUTHORIZATION_HEADER, loginId);
    cookie.setPath("/");
    cookie.setMaxAge(5 * 60);
    return cookie;
  }

  public void removeloginIdCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(LOGIN_ID_AUTHORIZATION_HEADER, null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  private String generateRandomCode() {
    // 랜덤한 6자리 숫자 생성
    Random random = new Random();
    int code = 100000 + random.nextInt(900000);
    return String.valueOf(code);
  }


}

