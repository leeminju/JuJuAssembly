package com.example.jujuassembly.domain.user.kakao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.kakao.KakaoService;
import com.example.jujuassembly.domain.user.kakao.KakaoUserInfoDto;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.EmailAuthUtil;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class KakaoServiceTest implements EmailAuthUtil {

  @InjectMocks
  KakaoService kakaoService;

  @Mock
  RestTemplate restTemplate;
  @Mock
  UserRepository userRepository;
  @Mock
  PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("카카오 로그인 getTokenTest메서드 테스트")
  void getTokenTest() throws Exception {
    // given
    String code = "testCode";
    String accessToken = "testAccessToken";
    String responseBody = "{ \"access_token\": \"" + accessToken + "\", \"expires_in\": 123 }";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, headers,
        HttpStatus.OK);

    when(restTemplate.exchange(any(RequestEntity.class), eq(String.class)))
        .thenReturn(responseEntity);

    // when
    String result = kakaoService.getToken(code);

    // then
    assertEquals(accessToken, result);
  }

  @Test
  @DisplayName("카카오 로그인 getKakaoUserInfoTest 메서드 테스트")
  void getKakaoUserInfoTest() throws Exception {
    // given
    String accessToken = "testAccessToken";
    Long id = TEST_USER_ID;
    String nickname = "testNickname";
    String email = "test@example.com";
    String imageUrl = "http://example.com/test.jpg";

    String responseBody = "{"
        + "\"id\":" + id + ","
        + "\"properties\":{\"nickname\":\"" + nickname + "\",\"profile_image\":\"" + imageUrl
        + "\"},"
        + "\"kakao_account\":{\"email\":\"" + email + "\"}"
        + "}";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, headers,
        HttpStatus.OK);

    when(restTemplate.exchange(any(RequestEntity.class), eq(String.class)))
        .thenReturn(responseEntity);

    // when
    KakaoUserInfoDto result = kakaoService.getKakaoUserInfo(accessToken);

    // then
    assertEquals(id, result.getId());
    assertEquals(nickname, result.getNickname());
    assertEquals(email, result.getEmail());
    assertEquals(imageUrl, result.getImageUrl());
  }

  @Test
  @DisplayName("카카오 로그인 registerKakaoUserIfNeededTest 메서드 테스트")
  void registerKakaoUserIfNeededTest() {
    // given
    KakaoUserInfoDto kakaoUserInfo = KakaoUserInfoDto.builder()
        .id(TEST_USER_ID)
        .nickname(TEST_USER_NICKNAME)
        .email(TEST_USER_EMAIL)
        .imageUrl(TEST_USER_IMAGE)
        .build();

    when(userRepository.findByKakaoId(eq(TEST_USER_ID))).thenReturn(Optional.empty());
    when(userRepository.findByEmail(eq(TEST_USER_EMAIL))).thenReturn(Optional.empty());

    // when
    User result = kakaoService.registerKakaoUserIfNeeded(kakaoUserInfo);

    // then
    assertNotNull(result);
    assertEquals(TEST_USER_EMAIL, result.getEmail());
    assertEquals(TEST_USER_NICKNAME, result.getNickname());
    assertEquals(TEST_USER_IMAGE, result.getImage());

    // Verify that save method is called once
    verify(userRepository, times(1)).save(any(User.class));
  }

}

