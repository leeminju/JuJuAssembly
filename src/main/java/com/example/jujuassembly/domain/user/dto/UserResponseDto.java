package com.example.jujuassembly.domain.user.dto;

import com.example.jujuassembly.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserResponseDto {

  private String loginId;
  private String nickname;
  private String email;

  public UserResponseDto(User user) {
    this.loginId = user.getLoginId();
    this.nickname = user.getNickname();
    this.email = user.getEmail();
  }
}
