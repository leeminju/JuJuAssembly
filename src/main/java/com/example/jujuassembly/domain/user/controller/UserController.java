package com.example.jujuassembly.domain.user.controller;

import com.example.jujuassembly.domain.user.dto.SingupRequestDto;
import com.example.jujuassembly.domain.user.dto.SignupResponseDto;
import com.example.jujuassembly.domain.user.service.UserService;
import com.example.jujuassembly.domain.user.service.emailAuth.EmailAuthService;
import com.example.jujuassembly.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
    SignupResponseDto signupResponseDto = userService.verificateCode(verificationCode, nickname, response);
    return ResponseEntity.ok()
        .body(new ApiResponse("회원가입 성공", HttpStatus.OK.value(), signupResponseDto));
  }

}
