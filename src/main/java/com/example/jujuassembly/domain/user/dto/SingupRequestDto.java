package com.example.jujuassembly.domain.user.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SingupRequestDto {

  private String loginId;
  private String nickname;
  private String password;
  private String passwordCheck;
  private Long firstPreferredCategoryId;
  private Long secondPreferredCategoryId;
  private String email;

}
