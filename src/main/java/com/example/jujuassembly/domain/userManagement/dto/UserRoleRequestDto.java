package com.example.jujuassembly.domain.userManagement.dto;

import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRoleRequestDto {

  private UserRoleEnum userRole;

}
