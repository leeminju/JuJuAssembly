package com.example.jujuassembly.domain.user.dto;

import com.example.jujuassembly.domain.category.entity.Category;
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
public class UserModifyRequestDto {

  private String nickname;
  private String password;
  private String passwordCheck;
  private Category firstPreferredCategoryId;
  private Category secondPreferredCategoryId;
  private String email;


}
