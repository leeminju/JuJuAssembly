package com.example.jujuassembly.domain.user.dto;

import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDetailResponseDto {

  private String loginId;
  private String nickname;
  private String password;
  private Long firstPreferredCategoryId;
  private Long secondPreferredCategoryId;
  private String email;
  private String image;
  private UserRoleEnum role;

  public UserDetailResponseDto(User user) {
    this.loginId = user.getLoginId();
    this.nickname = user.getNickname();
    this.password = user.getPassword();
    this.firstPreferredCategoryId = user.getId();
    this.secondPreferredCategoryId = user.getId();
    this.email = user.getEmail();
    this.image = user.getImage();
    this.role = user.getRole();
  }
}
