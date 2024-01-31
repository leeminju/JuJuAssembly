package com.example.jujuassembly.domain.userManagement.controller;

import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum.Authority;
import com.example.jujuassembly.domain.userManagement.dto.UserRoleRequestDto;
import com.example.jujuassembly.domain.userManagement.dto.UserRoleResponseDto;
import com.example.jujuassembly.domain.userManagement.service.UserManageService;
import com.example.jujuassembly.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/v1")
public class UserManageController {

  private final UserManageService userManageService;

  /**
   * 전체 사용자 조회 API
   *
   * @return 전체 사용자 목록을 포함한 ApiResponse
   */
  @GetMapping("/users")
  public ResponseEntity<ApiResponse<Page<UserDetailResponseDto>>> viewAllUsers(
      @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    Page<UserDetailResponseDto> allUserResponseDto = userManageService.viewAllUsers(pageable);
    return ResponseEntity.ok().body(
        new ApiResponse<>("전체 사용자 조회", HttpStatus.OK.value(), allUserResponseDto));
  }

  /**
   * 관리자 조회 API
   *
   * @return 관리자 목록을 포함한 ApiResponse
   */

  @GetMapping("/users/admin")
  public ResponseEntity<ApiResponse<Page<UserDetailResponseDto>>> viewAdminUsers(
      @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    Page<UserDetailResponseDto> adminUserResponseDto = userManageService.viewAdminUsers(pageable);
    return ResponseEntity.ok().body(
        new ApiResponse<>("전체 관리자 조회", HttpStatus.OK.value(), adminUserResponseDto));
  }

  /**
   * 회원 권한 수정 API
   *
   * @param userId             권한을 수정할 사용자의 ID
   * @param userRolerequestDto 수정할 권한 정보를 담은 DTO
   * @return 수정된 사용자 권한 정보를 포함한 ApiResponse
   */
  @Secured(Authority.ADMIN)
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
