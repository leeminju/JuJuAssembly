package com.example.jujuassembly.domain.emailauth.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.emailAuth.entity.EmailAuth;
import com.example.jujuassembly.domain.emailAuth.repository.EmailAuthRepository;
import com.example.jujuassembly.domain.emailAuth.service.EmailAuthService;
import com.example.jujuassembly.global.EmailAuthUtil;
import com.example.jujuassembly.global.mail.EmailService;
import java.util.Optional;
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
  EmailAuthRepository emailAuthRepository;
  @Mock
  RedisTemplate<String, String> redisTemplate;

  @DisplayName("인증코드 전송 테스트")
  @Test
  void checkVerifyVerificationCodeTest() {
    // given
    String loginId = TEST_USER_LOGINID;
    String verificationCode = TEST_SENTCODE;

    // when
    when(emailAuthRepository.findTopByLoginIdOrderByCreatedAtDesc(anyString()))
        .thenReturn(Optional.of(TEST_EMAILAUTH));
    when(redisTemplate.hasKey(anyString())).thenReturn(true);

    // then
    EmailAuth result = emailAuthService.checkVerifyVerificationCode(loginId, verificationCode);

    assertEquals(loginId, result.getLoginId());
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
