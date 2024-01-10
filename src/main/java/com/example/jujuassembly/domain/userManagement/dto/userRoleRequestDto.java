package com.example.jujuassembly.domain.userManagement.dto;

import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class userRoleRequestDto {

  private UserRoleEnum userRole;

}
