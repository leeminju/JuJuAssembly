package com.example.jujuassembly.domain.user.dto;

import com.example.jujuassembly.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class UserResponseDto {
  private Long id;
  private String loginId;
  private String nickname;
  private String email;
  private String image;

  public UserResponseDto(User user) {
    this.id = user.getId();
    this.loginId = user.getLoginId();
    this.nickname = user.getNickname();
    this.email = user.getEmail();
    this.image = user.getImage();
  }
}
