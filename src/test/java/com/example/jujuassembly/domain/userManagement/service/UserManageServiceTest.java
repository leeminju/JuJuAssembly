package com.example.jujuassembly.domain.userManagement.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.domain.userManagement.dto.UserRoleRequestDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class UserManageServiceTest {

  @Mock
  UserRepository userRepository;

  @InjectMocks
  UserManageService userManageService;


  @Test
  @DisplayName("관리자 권한으로 회원 전체 조회 테스트")
  void viewAllUsersTest() {
    //given
    Category category1 = Category.builder().id(1L).name("소주").image("아아아앙").build();
    User user1 = new User("user1", "user1", "user1Email", "12341234", category1, category1);
    User user2 = new User("user2", "user2", "user2Email", "12341234", category1, category1);

    List<UserResponseDto> mockAllUserResponseDto = new ArrayList<>();
    List<User> mockUsers = Arrays.asList(user1, user2);
    mockUsers.forEach(user -> {
      var userDto = new UserResponseDto(user);
      mockAllUserResponseDto.add(userDto);
    });

    when(userRepository.findAll()).thenReturn(mockUsers);

    List<UserResponseDto> result = userManageService.viewAllUsers();

    // then
    // assert that the size is equal
    assertThat(result.size(), is(equalTo(mockAllUserResponseDto.size())));
    assertThat(result.get(0).getNickname(),
        is(equalTo(mockAllUserResponseDto.get(0).getNickname())));
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

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    //when
    var result = userManageService.modifyUserRole(1L, userRoleRequestDto);

    //then
    assertThat(result.getUserRole(), is(equalTo(user.getRole().getAuthority())));

  }


}