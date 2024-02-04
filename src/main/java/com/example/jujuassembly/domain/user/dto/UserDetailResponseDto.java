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

  private Long id;
  private String loginId;
  private String nickname;
  private Long firstPreferredCategoryId;
  private Long secondPreferredCategoryId;
  private String email;
  private String image;
  private UserRoleEnum role;
  private Long kakaoId;

  public UserDetailResponseDto(User user) {
    this.id = user.getId();
    this.loginId = user.getLoginId();
    this.nickname = user.getNickname();
    if (user.getFirstPreferredCategory() != null) {
      this.firstPreferredCategoryId = user.getFirstPreferredCategory().getId();
    }
    if (user.getSecondPreferredCategory() != null) {
      this.secondPreferredCategoryId = user.getSecondPreferredCategory().getId();
    }
    this.email = user.getEmail();
    this.image = user.getImage();
    this.role = user.getRole();
    this.kakaoId = user.getKakaoId();
  }
}
