package com.example.jujuassembly.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.emailAuth.entity.EmailAuth;
import com.example.jujuassembly.domain.emailAuth.repository.EmailAuthRepository;
import com.example.jujuassembly.domain.emailAuth.service.EmailAuthService;
import com.example.jujuassembly.domain.user.dto.LoginRequestDto;
import com.example.jujuassembly.domain.user.dto.SignupRequestDto;
import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.dto.UserModifyRequestDto;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.EmailAuthUtil;
import com.example.jujuassembly.global.jwt.JwtUtil;
import com.example.jujuassembly.global.mail.EmailService;
import com.example.jujuassembly.global.s3.S3Manager;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest implements EmailAuthUtil {

  @InjectMocks
  UserService userService;
  @Mock
  UserRepository userRepository;
  @Mock
  JwtUtil jwtUtil;
  @Mock
  PasswordEncoder passwordEncoder;
  @Mock
  CategoryRepository categoryRepository;
  @Mock
  EmailAuthRepository emailAuthRepository;
  @Mock
  S3Manager s3Manager;
  @Mock
  ValueOperations valueOperations;
  @Mock
  RedisTemplate redisTemplate;
  @Mock
  EmailService emailService;
  @Mock
  EmailAuthService emailAuthService;

  @DisplayName("회원가입 이메일 전송 테스트")
  @Test
  void signupTest() {
    // given
    SignupRequestDto signupRequestDto = SignupRequestDto.builder()
        .loginId(TEST_USER_LOGINID)
        .nickname(TEST_USER_NICKNAME)
        .password(TEST_USER_PASSWORD)
        .passwordCheck(TEST_USER_PASSWORD)
        .firstPreferredCategoryId(TEST_USER_FIRSTPREFERRED_CATEGORY.getId())
        .secondPreferredCategoryId(TEST_USER_SECONDPREFERRED_CATEGORY.getId())
        .email(TEST_USER_EMAIL)
        .build();

    // when-then
    assertEquals(TEST_USER_LOGINID, signupRequestDto.getLoginId());
    assertEquals(TEST_USER_NICKNAME, signupRequestDto.getNickname());
    assertEquals(TEST_USER_PASSWORD, signupRequestDto.getPassword());
    assertEquals(TEST_USER_FIRSTPREFERRED_CATEGORY.getId(), signupRequestDto.getFirstPreferredCategoryId());
    assertEquals(TEST_USER_SECONDPREFERRED_CATEGORY.getId(), signupRequestDto.getSecondPreferredCategoryId());
    assertEquals(TEST_USER_EMAIL, signupRequestDto.getEmail());
  }

  @DisplayName("인증번호로 회원가입 테스트")
  @Test
  void verificateCodeTest() {
    // given
    EmailAuth emailAuth = new EmailAuth(TEST_USER_LOGINID, TEST_USER_NICKNAME, TEST_USER_EMAIL,
        TEST_USER_PASSWORD, TEST_CATEGORY_ID, TEST_ANOTHER_CATEGORY_ID, TEST_SENTCODE);
    String loginId = TEST_USER_LOGINID;

    // when-then
    String getNicknameResult = emailAuth.getNickname();
    String getEmailResult = emailAuth.getEmail();
    String getPasswordResult = emailAuth.getPassword();
    Long getFirstPreferredCategoryIdResult = emailAuth.getFirstPreferredCategoryId();
    Long getSecondPreferredCategoryIdResult = emailAuth.getSecondPreferredCategoryId();

    User userResult = new User(loginId, getNicknameResult, getEmailResult, getPasswordResult,
        TEST_CATEGORY, TEST_ANOTHER_CATEGORY);

    UserResponseDto userResponseDtoResult = new UserResponseDto(userResult);

    assertEquals(TEST_USER_NICKNAME, getNicknameResult);
    assertEquals(TEST_USER_EMAIL, getEmailResult);
    assertEquals(TEST_USER_PASSWORD, getPasswordResult);
    assertEquals(TEST_USER_FIRSTPREFERRED_CATEGORY.getId(), getFirstPreferredCategoryIdResult);
    assertEquals(TEST_USER_SECONDPREFERRED_CATEGORY.getId(), getSecondPreferredCategoryIdResult);

    assertEquals(userResponseDtoResult.getNickname(), TEST_USER_NICKNAME);
    assertEquals(userResponseDtoResult.getEmail(), TEST_USER_EMAIL);
    assertEquals(userResponseDtoResult.getLoginId(), TEST_USER_LOGINID);
  }

  @DisplayName("로그인 테스트")
  @Test
  void loginTest() {
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

    String result = userService.login(loginRequestDto);

    // then
    assertThat(result).isEqualTo("mockedAccessToken");

  }

  @Test
  @DisplayName("프로필 조회 테스트")
  void testViewMyProfile() {
    // 가상의 사용자 객체 생성
    Long userId = 123L;
    Category category1 = Category.builder().id(1L).name("소주").build();
    Category category2 = Category.builder().id(2L).name("맥주").build();
    User mockUser = User.builder().id(userId).loginId("user").nickname("user").email("email")
        .password("1234").isArchived(false).role(UserRoleEnum.USER).image("aaa").kakaoId(2L)
        .firstPreferredCategory(category1).secondPreferredCategory(category2).build();

    // when
    // UserService의 viewMyProfile 메서드 호출
    UserDetailResponseDto resultDto = userService.viewMyProfile(mockUser);

    // then
    assertThat(resultDto).isNotNull();
    assertThat(resultDto.getId()).isEqualTo(mockUser.getId());
    assertThat(resultDto.getLoginId()).isEqualTo(mockUser.getLoginId());
    assertThat(resultDto.getNickname()).isEqualTo(mockUser.getNickname());
    assertThat(resultDto.getEmail()).isEqualTo(mockUser.getEmail());
    //assertThat(resultDto.getRole()).isEqualTo(mockUser.getRole());
    assertThat(resultDto.getImage()).isEqualTo(mockUser.getImage());
  }


  @Test
  @DisplayName("프로필 수정 테스트")
  void modifyProfileTest() {
    // given
    Long userId = 123L;
    Category category1 = Category.builder().id(1L).name("소주").build();
    Category category2 = Category.builder().id(2L).name("맥주").build();
    User user = User.builder().id(userId).loginId("user").nickname("user")
        .firstPreferredCategory(category1)
        .secondPreferredCategory(category2).email("email").build();

    UserModifyRequestDto modifyRequestDto = UserModifyRequestDto.builder().currentPassword("1234")
        .password("password").passwordCheck("password").nickname("modifiedUser")
        .firstPreferredCategoryId(1L)
        .secondPreferredCategoryId(2L)
        .email("modifiedEmail").build();

    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    when(passwordEncoder.matches(any(), any())).thenReturn(true);
    when(userRepository.findUserByIdOrElseThrow(userId)).thenReturn(user);
    when(categoryRepository.findCategoryByIdOrElseThrow(any())).thenReturn(category1);

    UserService userService = new UserService(
        userRepository, passwordEncoder, mock(EmailAuthService.class),
        categoryRepository, mock(EmailAuthRepository.class), mock(JwtUtil.class),
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
    Category category1 = Category.builder().id(1L).name("소주").build();
    Category category2 = Category.builder().id(2L).name("맥주").build();
    User user = User.builder().id(userId).firstPreferredCategory(category1)
        .secondPreferredCategory(category2).build();

    // 이미지 파일 생성
    byte[] content = "fakeImage".getBytes();
    MultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", content);

    String mockImageUrl = "https://example.com/image.jpg";

    when(userRepository.findUserByIdOrElseThrow(userId)).thenReturn(user);
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