package com.example.jujuassembly.domain.userManagement.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.domain.userManagement.dto.UserRoleRequestDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@ExtendWith(MockitoExtension.class)
class UserManageServiceTest {

  @Mock
  UserRepository userRepository;

  @Mock
  Pageable pageable;

  @InjectMocks
  UserManageService userManageService;

  @Test
  @DisplayName("관리자 권한으로 회원 전체 조회 테스트 + 페이징 테스트")
  void viewAllUsersTest() {
    //given
    pageable = PageRequest.of(0, 10); // 예시 페이지 요청

    //User 리스트 생성 및 초기화
    List<User> allUserList = new ArrayList<>();
    Category category1 = Category.builder().id(1L).name("소주").image("아아아앙").build();
    User user1 = new User("user1", "user1", "user1Email", "12341234", category1, category1);
    User user2 = new User("user2", "user2", "user2Email", "12341234", category1, category1);
    allUserList.add(user1);
    allUserList.add(user2);

    Page<User> allUserPage = new PageImpl<>(allUserList, pageable, allUserList.size());

    //when
    when(userRepository.findAll(pageable)).thenReturn(allUserPage);

    //서비스 메서드 호
    Page<UserDetailResponseDto> result = userManageService.viewAllUsers(pageable);

    // then
    Assertions.assertNotNull(result);
    assertEquals(allUserList.size(), result.getContent().size());

  }

  @Test
  @DisplayName("회원 권한 수정 테스트")
  void changeUserRoleTest() {
    //given
    Category category1 = Category.builder().id(1L).name("소주").image("아아아앙").build();
    User user = User.builder().id(1L).loginId("user1").nickname("user1").email("user1Email")
        .password("12341234").firstPreferredCategory(category1).secondPreferredCategory(category1)
        .build();
    UserRoleRequestDto userRoleRequestDto = UserRoleRequestDto.builder()
        .userRole(UserRoleEnum.ADMIN).build();

    when(userRepository.findUserByIdOrElseThrow(1L)).thenReturn(user);

    //when
    var result = userManageService.modifyUserRole(1L, userRoleRequestDto);

    //then
    assertThat(result.getUserRole(), is(equalTo(user.getRole().getAuthority())));

  }

  @Test
  @DisplayName("관리자 전체 조회 테스트 + 페이징 테스트")
  void viewAdminUsersTest() {
    // Mock 데이터 설정
    User adminUser = User.builder().id(1L).nickname("nick").role(UserRoleEnum.ADMIN).build();

    Pageable pageable = PageRequest.of(0, 10);

    // userRepository.findAllByRole()이 호출될 때 모의 데이터를 반환하도록 설정
    when(userRepository.findAllByRole(pageable, UserRoleEnum.ADMIN))
        .thenReturn(new PageImpl<>(Collections.singletonList(adminUser)));

    // 테스트 대상 메서드 호출
    Page<UserDetailResponseDto> resultPage = userManageService.viewAdminUsers(pageable);

    // 예상 결과 생성
    UserDetailResponseDto expectedDto = new UserDetailResponseDto(adminUser);

    // 결과 검증
    assertEquals(1, resultPage.getTotalElements());
    assertEquals(expectedDto.getNickname(), resultPage.getContent().get(0).getNickname());

    // userRepository.findAllByRole()이 호출되었는지 검증
    verify(userRepository, times(1)).findAllByRole(pageable, UserRoleEnum.ADMIN);

  }


}