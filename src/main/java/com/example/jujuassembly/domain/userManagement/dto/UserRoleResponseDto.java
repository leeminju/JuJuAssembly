package com.example.jujuassembly.domain.userManagement.dto;

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
public class UserRoleResponseDto {

  private String loginId;
  private String nickname;
  private String email;
  private String userRole;

  public UserRoleResponseDto(User user) {
    this.loginId = user.getLoginId();
    this.nickname = user.getNickname();
    this.email = user.getEmail();
    this.userRole = user.getRole().getAuthority();
  }
}
