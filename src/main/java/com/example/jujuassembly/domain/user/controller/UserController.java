package com.example.jujuassembly.domain.user.controller;

import com.example.jujuassembly.domain.user.dto.LoginRequestDto;
import com.example.jujuassembly.domain.user.dto.SingupRequestDto;
import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.dto.UserModifyRequestDto;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.service.UserService;
import com.example.jujuassembly.domain.user.service.emailAuth.EmailAuthService;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j (topic = "user_controller")
@RequiredArgsConstructor
@RequestMapping("/v1")
public class UserController {

  private final UserService userService;

  // 회원가입, 이메일 발송
  @PostMapping("/auth/signup")
  public ResponseEntity<ApiResponse> siginup(
      @Valid @RequestBody SingupRequestDto singupRequestDto, HttpServletResponse response) {
    //validation 검증 추가

    userService.signup(singupRequestDto, response);
    return ResponseEntity.ok(new ApiResponse<>("인증 번호를 입력해주세요.", HttpStatus.OK.value()));
  }

  // 인증번호 받는 API -> 회원가입 완료 후 DB에 user 정보 저장
  @GetMapping("/auth/signup")
  public ResponseEntity<ApiResponse> verificateCode(
      @RequestHeader("verificationCode") String verificationCode,
      @CookieValue(EmailAuthService.NICkNAME_AUTHORIZATION_HEADER) String nickname,
      HttpServletResponse response) {
    UserResponseDto userResponseDto = userService.verificateCode(verificationCode, nickname,
        response);
    return ResponseEntity.ok()
        .body(new ApiResponse("회원가입 성공", HttpStatus.OK.value(), userResponseDto));
  }

  // 로그인
  @PostMapping("/auth/login")
  public ResponseEntity<ApiResponse> login(@RequestBody LoginRequestDto requestDto,
      HttpServletResponse response) {
    UserResponseDto userResponseDto = userService.login(requestDto, response);
    return ResponseEntity.ok()
        .body(new ApiResponse("로그인 성공", HttpStatus.OK.value(), userResponseDto));
  }

  //프로필 조회 (연우)
  @GetMapping ("/users/{userId}")
  public ResponseEntity<ApiResponse> viewProfile(@PathVariable Long userId,
      @AuthenticationPrincipal UserDetailsImpl userDetails){
    UserDetailResponseDto responseDto = userService.viewProfile(userId, userDetails.getUser());
    return ResponseEntity.ok()
        .body(new ApiResponse("프로필 조회", HttpStatus.OK.value(),responseDto));
  }

  //프로실 수정
  @PatchMapping ("/users/{userId}")
  public ResponseEntity<ApiResponse> updatePofile(@PathVariable Long userId,
      @RequestBody UserModifyRequestDto modifyRequestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails){
    log.info("test");
    UserDetailResponseDto responseDto = userService.modifyProfile(userId, userDetails.getUser(), modifyRequestDto);
    return ResponseEntity.ok()
        .body(new ApiResponse("프로필 수정 완료", HttpStatus.OK.value(),responseDto));
  }

  //이미지 추가
  @PostMapping("/users/{userId}")
  public ResponseEntity<ApiResponse> addImage(@PathVariable Long userId,
      @RequestParam("image") MultipartFile image) throws Exception{
    UserDetailResponseDto responseDto= userService.uploadImage(userId, image);
    return ResponseEntity.ok()
        .body(new ApiResponse<>("사진 추가 성공", HttpStatus.OK.value(), responseDto));
  }
}
