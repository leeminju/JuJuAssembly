package com.example.jujuassembly.global;

import com.example.jujuassembly.domain.emailAuth.entity.EmailAuth;
import com.example.jujuassembly.domain.emailAuth.service.EmailAuthService;

public interface EmailAuthUtil extends UserTestUtil {

  Long TEST_EMAILAUTH_ID = 1L;
  String TEST_SENTCODE = "sent-code";
  String TEST_VERIFICATION_CODE_HEADER = EmailAuthService.VERIFICATION_CODE_HEADER;
  String TEST_LOGIN_ID_AUTHORIZATION_HEADER = EmailAuthService.LOGIN_ID_AUTHORIZATION_HEADER;

  EmailAuth TEST_EMAILAUTH = EmailAuth.builder()
      .id(TEST_EMAILAUTH_ID)
      .loginId(TEST_USER_LOGINID)
      .nickname(TEST_USER_NICKNAME)
      .email(TEST_USER_EMAIL)
      .password(TEST_USER_PASSWORD)
      .firstPreferredCategoryId(TEST_CATEGORY_ID)
      .secondPreferredCategoryId(TEST_ANOTHER_CATEGORY_ID)
      .sentCode(TEST_SENTCODE)
      .build();

}
