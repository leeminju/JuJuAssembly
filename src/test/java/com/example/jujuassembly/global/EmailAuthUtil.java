package com.example.jujuassembly.global;

import com.example.jujuassembly.domain.user.emailAuth.service.EmailAuthService;

public interface EmailAuthUtil extends UserTestUtil {

  String TEST_SENTCODE = "sent-code";
  String TEST_VERIFICATION_CODE_HEADER = EmailAuthService.VERIFICATION_CODE_HEADER;
  String TEST_LOGIN_ID_AUTHORIZATION_HEADER = EmailAuthService.LOGIN_ID_AUTHORIZATION_HEADER;

}
