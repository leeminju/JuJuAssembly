package com.example.jujuassembly.domain.user.emailAuth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailAuthDto {

  private String loginId;
  private String nickname;
  private String email;
  private String password;
  private Long firstPreferredCategoryId;
  private Long secondPreferredCategoryId;

  public EmailAuthDto(String loginId, String nickname, String email, String password,
      Long firstPreferredCategoryId, Long secondPreferredCategoryId) {
    this.loginId = loginId;
    this.nickname = nickname;
    this.email = email;
    this.password = password;
    this.firstPreferredCategoryId = firstPreferredCategoryId;
    this.secondPreferredCategoryId = secondPreferredCategoryId;
  }
}
