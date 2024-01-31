package com.example.jujuassembly.domain.emailauth.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import com.example.jujuassembly.domain.user.emailAuth.dto.EmailAuthDto;
import com.example.jujuassembly.domain.user.emailAuth.service.EmailAuthService;
import com.example.jujuassembly.global.EmailAuthUtil;
import com.example.jujuassembly.global.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class EmailAuthServiceTest implements EmailAuthUtil {

  @InjectMocks
  EmailAuthService emailAuthService;
  @Mock
  EmailService emailService;
  @Mock
  RedisTemplate<String, String> redisTemplate;

  @DisplayName("인증코드 전송 테스트")
  @Test
  void checkVerifyVerificationCodeTest() {
    // given
    String loginId = TEST_USER_LOGINID;
    String nickname = TEST_USER_NICKNAME;
    String email = TEST_USER_EMAIL;
    String password = TEST_USER_PASSWORD;
    Long firstPreferredCategoryId = TEST_CATEGORY_ID;
    Long secondPreferredCategoryId = TEST_ANOTHER_CATEGORY_ID;

    // when
    EmailAuthDto result = new EmailAuthDto(loginId, nickname, email, password,
        firstPreferredCategoryId,
        secondPreferredCategoryId);

    // then
    assertEquals(result.getLoginId(), TEST_USER_LOGINID);
    assertEquals(result.getNickname(), TEST_USER_NICKNAME);
    assertEquals(result.getEmail(), TEST_USER_EMAIL);
    assertEquals(result.getPassword(), TEST_USER_PASSWORD);
    assertEquals(result.getFirstPreferredCategoryId(), TEST_CATEGORY_ID);
    assertEquals(result.getSecondPreferredCategoryId(), TEST_ANOTHER_CATEGORY_ID);
  }

  @DisplayName("인증코드 전송시 인증코드 생성 테스트")
  @Test
  void sendVerificationCodeTest() {
    // given
    String email = TEST_USER_EMAIL;

    // when
    doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

    // then
    String result = emailAuthService.sendVerificationCode(email);

    assertEquals(true, result != null);
    assertEquals(true, result.matches("\\d{6}"));
  }

}
