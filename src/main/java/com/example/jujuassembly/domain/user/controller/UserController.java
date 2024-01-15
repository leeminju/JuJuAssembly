package com.example.jujuassembly.domain.user.controller;

import com.example.jujuassembly.domain.emailAuth.service.EmailAuthService;
import com.example.jujuassembly.domain.user.dto.LoginRequestDto;
import com.example.jujuassembly.domain.user.dto.SignupRequestDto;
import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.dto.UserModifyRequestDto;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.service.UserService;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j(topic = "user_controller")
@RequiredArgsConstructor
@RequestMapping("/v1")
public class UserController {

  private final UserService userService;

  /**
   * 회원가입, 이메일 발송
   *
   * @param signupRequestDto 회원가입 정보를 담은 DTO
   * @param response         HttpServletResponse로 받은 객체
   * @return 회원가입 성공여부를 담은 ApiResponse
   */
  @PostMapping("/auth/signup")
  public ResponseEntity<ApiResponse> siginup(
      @Valid @RequestBody SignupRequestDto signupRequestDto, HttpServletResponse response) {

    userService.signup(signupRequestDto, response);
    return ResponseEntity.ok(new ApiResponse<>("인증 번호를 입력해주세요.", HttpStatus.OK.value()));
  }


  /**
   * 회원가입 인증번호 받는 API -> 회원가입 완료 후 DB에 user 정보 저장
   *
   * @param verificationCode 전송된 인증번호
   * @param loginId          로그인 ID 쿠키 값
   * @param response         HttpServletResponse 객체
   * @return 회원가입 성공여부를 담은 ApiResponse
   */
  @GetMapping("/auth/signup")
  public ResponseEntity<ApiResponse> verificateCode(
      @RequestHeader("verificationCode") String verificationCode,
      @CookieValue(EmailAuthService.LOGIN_ID_AUTHORIZATION_HEADER) String loginId,
      HttpServletResponse response) {
    UserResponseDto userResponseDto = userService.verificateCode(verificationCode, loginId,
        response);
    return ResponseEntity.ok()
        .body(new ApiResponse("회원가입 성공", HttpStatus.OK.value(), userResponseDto));
  }

  /**
   * 로그인 API
   *
   * @param requestDto 로그인 정보를 담은 DTO
   * @param response   HttpServletResponse 객체
   * @return 로그인 성공 여부를 담은 ApiResponse
   */
  @PostMapping("/auth/login")
  public ResponseEntity<ApiResponse> login(@RequestBody LoginRequestDto requestDto,
      HttpServletResponse response) {
    UserResponseDto userResponseDto = userService.login(requestDto, response);
    return ResponseEntity.ok()
        .body(new ApiResponse("로그인 성공", HttpStatus.OK.value(), userResponseDto));
  }

  /**
   * 로그아웃 API
   *
   * @param request HttpServletRequest 객체
   * @return 로그아웃 성공 여부를 담은 ApiResponse
   */
  @PostMapping("/users/logout")
  public ResponseEntity<ApiResponse> logout(HttpServletRequest request,
      HttpServletResponse response) {
    userService.logout(request, response);
    return ResponseEntity.ok().body(new ApiResponse("로그아웃 성공", HttpStatus.OK.value()));
  }

  /**
   * 프로필 조회 API
   *
   * @param userId      조회할 유저의 ID
   * @param userDetails 인증된 사용자의 UserDetailsImpl
   * @return 프로필조회 성공 여부를 담은 ApiResponse
   */
  @GetMapping("/users/{userId}")
  public ResponseEntity<ApiResponse> viewProfile(@PathVariable Long userId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    UserDetailResponseDto responseDto = userService.viewProfile(userId, userDetails.getUser());
    return ResponseEntity.ok()
        .body(new ApiResponse("프로필 조회", HttpStatus.OK.value(), responseDto));
  }

  @GetMapping("/users/myprofile")
  public ResponseEntity<ApiResponse> viewMyProfile(
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    UserDetailResponseDto responseDto = userService.viewMyProfile(userDetails.getUser());
    return ResponseEntity.ok()
        .body(new ApiResponse("내 프로필 조회", HttpStatus.OK.value(), responseDto));
  }

  /**
   * 프로필 수정 API
   *
   * @param userId           수정할 유저의 ID
   * @param modifyRequestDto 수정할 정보를 담은 DTO
   * @param userDetails      인증된 사용자의 UserDetailsImpl 객체
   * @return 프로필수정 성공 여부를 담은 ApiResponse
   */
  @PatchMapping("/users/{userId}")
  public ResponseEntity<ApiResponse> updatePofile(@PathVariable Long userId,
      @RequestBody UserModifyRequestDto modifyRequestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    UserDetailResponseDto responseDto = userService.modifyProfile(userId, userDetails.getUser(),
        modifyRequestDto);
    return ResponseEntity.ok()
        .body(new ApiResponse("프로필 수정 완료", HttpStatus.OK.value(), responseDto));
  }

  /**
   * 이미지 추가 API
   *
   * @param userId 수정할 유저의 ID
   * @param image  추가할 이미지 파일
   * @return 이미지 추가 여부를 반환하는 ApiResponse
   * @throws Exception 이미지 업로드 시 발생할 수 있는 예외
   */
  @PostMapping("/users/{userId}")
  public ResponseEntity<ApiResponse> addImage(@PathVariable Long userId,
      @RequestParam MultipartFile image) throws Exception {
    UserDetailResponseDto responseDto = userService.uploadImage(userId, image);
    return ResponseEntity.ok()
        .body(new ApiResponse("사진 추가 성공", HttpStatus.OK.value(), responseDto));
  }

  /**
   * 회원 탈퇴 API
   *
   * @param userId      탈퇴할 유저의 ID
   * @param password    사용자 비밀번호
   * @param userDetails 인증된 사용자의 UserDetailsImpl 객체
   * @return 탈퇴 여부 ApiResponse
   */
  @DeleteMapping("/users/{userId}")
  public ResponseEntity<ApiResponse> deleteAccount(@PathVariable Long userId,
      @RequestHeader("Password") String password,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    userService.deleteAccount(userId, password, userDetails);
    return ResponseEntity.ok().body(new ApiResponse("회원 탈퇴 성공", HttpStatus.OK.value()));
  }


}
