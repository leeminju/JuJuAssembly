package com.example.jujuassembly.domain.userManagement.controller;

import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum.Authority;
import com.example.jujuassembly.domain.userManagement.dto.UserRoleRequestDto;
import com.example.jujuassembly.domain.userManagement.dto.UserRoleResponseDto;
import com.example.jujuassembly.domain.userManagement.service.UserManageService;
import com.example.jujuassembly.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Secured(Authority.ADMIN)
@RequestMapping("/v1")
public class UserManageController {

  private final UserManageService userManageService;

  /**
   * 전체 사용자 조회 API
   *
   * @return 전체 사용자 목록을 포함한 ApiResponse
   */
  @GetMapping("/users")
  public ResponseEntity<ApiResponse<List<UserDetailResponseDto>>> viewAllUsers() {
    List<UserDetailResponseDto> allUserResponseDtoList = userManageService.viewAllUsers();
    return ResponseEntity.ok().body(
        new ApiResponse<>("전체 사용자 조회", HttpStatus.OK.value(), allUserResponseDtoList));
  }

  /**
   * 회원 권한 수정 API
   *
   * @param userId             권한을 수정할 사용자의 ID
   * @param userRolerequestDto 수정할 권한 정보를 담은 DTO
   * @return 수정된 사용자 권한 정보를 포함한 ApiResponse
   */
  @PatchMapping("/users/{userId}/role")
  public ResponseEntity<ApiResponse<UserRoleResponseDto>> modifyUserRole(
      @PathVariable Long userId,
      @RequestBody UserRoleRequestDto userRolerequestDto) {
    UserRoleResponseDto userRoleResponseDto = userManageService.modifyUserRole(userId,
        userRolerequestDto);
    return ResponseEntity.ok().body(
        new ApiResponse<>("사용자 권한 수정 완료", HttpStatus.OK.value(), userRoleResponseDto));
  }


}
