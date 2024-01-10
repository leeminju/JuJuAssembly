package com.example.jujuassembly.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.emailAuth.repository.EmailAuthRepository;
import com.example.jujuassembly.domain.emailAuth.service.EmailAuthService;
import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.dto.UserModifyRequestDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.jwt.JwtUtil;
import com.example.jujuassembly.global.s3.S3Manager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  S3Manager s3Manager;

  @Mock
  UserRepository userRepository;

  @InjectMocks
  UserService userService;

  @Test
  @DisplayName("프로필 조회 테스트")
  void viewProfileTest() {
    // given
    Long userId = 123L;
    User user = User.builder().id(userId).loginId("user").nickname("user").email("email").build();

    UserRepository userRepository = mock(UserRepository.class);
    when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

    UserService userService = new UserService(
        userRepository, mock(PasswordEncoder.class), mock(EmailAuthService.class),
        mock(CategoryRepository.class), mock(EmailAuthRepository.class), mock(JwtUtil.class),
        mock(S3Manager.class)
    );

    // when
    UserDetailResponseDto result = userService.viewProfile(userId, user);

    // then
    assertNotNull(result);
    assertEquals(user.getLoginId(), result.getLoginId());
    assertEquals(user.getNickname(), result.getNickname());
    assertEquals(user.getEmail(), result.getEmail());
  }


  @Test
  @DisplayName("프로필 수정 테스트")
  void modifyProfileTest() {
    // given
    Long userId = 123L;
    User user = User.builder().id(userId).loginId("user").nickname("user").email("email").build();

    UserModifyRequestDto modifyRequestDto = UserModifyRequestDto.builder().nickname("modifiedUser")
        .email("modifiedEmail").build();

    UserRepository userRepository = mock(UserRepository.class);
    when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

    UserService userService = new UserService(
        userRepository, mock(PasswordEncoder.class), mock(EmailAuthService.class),
        mock(CategoryRepository.class), mock(EmailAuthRepository.class), mock(JwtUtil.class),
        mock(S3Manager.class)
    );

    //when
    UserDetailResponseDto result = userService.modifyProfile(userId, user, modifyRequestDto);

    // then
    assertNotNull(result);

    assertEquals(user.getLoginId(), result.getLoginId());
    assertEquals(user.getNickname(), result.getNickname());
    assertEquals(user.getEmail(), result.getEmail());

  }

  @Test
  @DisplayName("사진 업로드 테스트")
  void uploadImageTest() throws Exception {
    //given
    Long userId = 1L;
    User user = User.builder().id(userId).build();

    // 이미지 파일 생성
    byte[] content = "fakeImage".getBytes();
    MultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", content);

    String mockImageUrl = "https://example.com/image.jpg";

    when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
    doNothing().when(s3Manager).deleteAllImageFiles(anyString(), anyString());
    when(s3Manager.upload(any(), eq("users"), eq(user.getId()))).thenReturn(mockImageUrl);

    //when
    UserDetailResponseDto result = userService.uploadImage(userId, image);

    //then
    // 이미지가 업로드되었는지 확인
    verify(s3Manager, times(1)).upload(eq(image), eq("users"), eq(user.getId()));
    assertEquals(mockImageUrl, result.getImage());
  }
}