package com.example.jujuassembly.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.user.dto.LoginRequestDto;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.UserTestUtil;
import com.example.jujuassembly.global.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest implements UserTestUtil {

  @InjectMocks
  UserService userService;
  @Mock
  UserRepository userRepository;
  @Mock
  JwtUtil jwtUtil;
  @Mock
  PasswordEncoder passwordEncoder;

  @DisplayName("로그인 테스트")
  @Test
  void login() {
    // given
    User user = TEST_USER;
    LoginRequestDto loginRequestDto
        = LoginRequestDto.builder()
        .loginId(TEST_USER_LOGINID)
        .password(TEST_USER_PASSWORD)
        .build();

    // when
    when(userRepository.findByLoginId(TEST_USER_LOGINID)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())).thenReturn(
        true);

    when(jwtUtil.createAccessToken(TEST_USER_LOGINID)).thenReturn("mockedAccessToken");
    when(jwtUtil.createRefreshToken(TEST_USER_LOGINID)).thenReturn("mockedRefreshToken");

    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    UserResponseDto result = userService.login(loginRequestDto, mockResponse);

    // then
    UserResponseDto expect = new UserResponseDto(TEST_USER);
    assertThat(result).isEqualTo(expect);

    // setHeader 메서드가 JwtUtil.AUTHORIZATION_HEADER와 "mockedAccessToken"이라는 매개변수로 호출되었는지를 검증
    verify(mockResponse).setHeader(eq(JwtUtil.AUTHORIZATION_HEADER), eq("mockedAccessToken"));
  }


}
