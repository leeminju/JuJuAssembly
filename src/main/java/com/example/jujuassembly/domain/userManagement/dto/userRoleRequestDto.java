package com.example.jujuassembly.domain.userManagement.dto;

import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
public class userRoleRequestDto {

  private UserRoleEnum userRole;

}
