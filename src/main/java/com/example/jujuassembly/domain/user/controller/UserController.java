package com.example.jujuassembly.domain.user.controller;

import com.example.jujuassembly.domain.user.emailAuth.service.EmailAuthService;
import com.example.jujuassembly.domain.user.dto.LoginRequestDto;
import com.example.jujuassembly.domain.user.dto.SignupRequestDto;
import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.dto.UserModifyRequestDto;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.kakao.KakaoService;
import com.example.jujuassembly.domain.user.service.UserService;
import com.example.jujuassembly.global.jwt.JwtUtil;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.filter.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  private final KakaoService kakaoService;
  private final EmailAuthService emailAuthService;
  private final JwtUtil jwtUtil;

  /**
   * 회원가입, 이메일 발송
   *
   * @param signupRequestDto 회원가입 정보를 담은 DTO
   * @param response         HttpServletResponse로 받은 객체
   * @return 회원가입 성공여부를 담은 ApiResponse
   */
  @PostMapping("/auth/signup")
  public ResponseEntity<ApiResponse> signupAsync(
      @Valid @RequestBody SignupRequestDto signupRequestDto,
      HttpServletResponse response) {

    userService.signup(signupRequestDto);

    String loginId = signupRequestDto.getLoginId();
    Cookie cookie = emailAuthService.getCookieByLoginId(loginId);
    response.addCookie(cookie);

    // 클라이언트에 먼저 응답 보내기
    return ResponseEntity.ok(new ApiResponse<>("인증 번호를 입력해주세요.", HttpStatus.OK.value()));
  }

  /**
   * 회원가입 인증번호 받는 API -> 회원가입 완료 후 DB에 user 정보 저장
   *
   * @param request  HttpServletRequest 객체
   * @param response HttpServletResponse 객체
   * @return 회원가입 성공여부를 담은 ApiResponse
   */

  @GetMapping("/auth/signup")
  public ResponseEntity<ApiResponse> verificateCode(
      HttpServletRequest request,
      HttpServletResponse response,
      @CookieValue(EmailAuthService.LOGIN_ID_AUTHORIZATION_HEADER) String loginId) {

    String verificationCode = request.getHeader(EmailAuthService.VERIFICATION_CODE_HEADER);

    UserResponseDto userResponseDto = userService.verificateCode(verificationCode, loginId);
    emailAuthService.removeloginIdCookie(response);

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
  public ResponseEntity<ApiResponse> login(
      @RequestBody LoginRequestDto requestDto,
      HttpServletResponse response) {
    String accessToken = userService.login(requestDto);

    Cookie cookie = jwtUtil.addJwtToCookie(accessToken);
    response.addCookie(cookie);

    return ResponseEntity.ok()
        .body(new ApiResponse("로그인 성공", HttpStatus.OK.value()));
  }

  /**
   * 내 프로필 조회 API
   *
   * @param userDetails 인증된 사용자의 UserDetailsImpl
   * @return 프로필조회 성공 여부를 담은 ApiResponse
   */
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
  public ResponseEntity<ApiResponse> updatePofile(
      @PathVariable Long userId,
      @Valid @RequestBody UserModifyRequestDto modifyRequestDto,
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
  public ResponseEntity<ApiResponse> addImage(
      @PathVariable Long userId,
      @RequestParam MultipartFile image) throws Exception {
    UserDetailResponseDto responseDto = userService.uploadImage(userId, image);
    return ResponseEntity.ok()
        .body(new ApiResponse("프로필 사진 변경 성공", HttpStatus.OK.value(), responseDto));
  }

  /**
   * 회원 탈퇴 API
   *
   * @param userId  탈퇴할 유저의 ID
   * @param request HttpServletRequest 객체
   * @return 탈퇴 여부 ApiResponse
   */
  @Transactional
  @DeleteMapping("/users/{userId}")
  public ResponseEntity<ApiResponse> deleteAccount(
      @PathVariable Long userId,
      HttpServletRequest request,
      HttpServletResponse response,
      @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

    // 회원탈퇴
    String password = request.getHeader("Password");
    userService.deleteAccount(userId, password, userDetails);

    // 로그아웃
    String loginId = userDetails.getUser().getLoginId();
    userService.logout(loginId, response);
    Cookie cookie = new Cookie(JwtUtil.AUTHORIZATION_HEADER, "account-deleted");
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);

    return ResponseEntity.ok().body(new ApiResponse("회원 탈퇴 성공", HttpStatus.OK.value()));
  }

  /**
   * 카카오 로그인 요청
   *
   * @param code 카카오에서 전달해주는 인증코드
   * @param response HttpServletResponse 객체
   * @return 홈페이지로 리다이렉트
   * @throws JsonProcessingException
   */
  @GetMapping("/auth/kakao/callback")
  public ResponseEntity<ApiResponse> kakaoLogin(
      @RequestParam String code,
      HttpServletResponse response) throws JsonProcessingException {

    String accessToken = kakaoService.kakaoLogin(code);

    Cookie cookie = jwtUtil.addJwtToCookie(accessToken);
    response.addCookie(cookie);

    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION,
            "/")
        .body(new ApiResponse("카카오 로그인 성공 및 리다이렉트", HttpStatus.FOUND.value()));
  }


}
