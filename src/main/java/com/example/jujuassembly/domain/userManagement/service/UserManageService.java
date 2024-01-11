package com.example.jujuassembly.domain.userManagement.service;

import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.domain.userManagement.dto.UserRoleRequestDto;
import com.example.jujuassembly.domain.userManagement.dto.UserRoleResponseDto;
import com.example.jujuassembly.global.exception.ApiException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserManageService {

  private final UserRepository userRepository;


  //전체 유저 조회
  public List<UserResponseDto> viewAllUsers() {

    List<User> allUserList = userRepository.findAll();
    List<UserResponseDto> allUserResponseDto = new ArrayList<>();

    allUserList.forEach(user -> {
      var userDto = new UserResponseDto(user);
      allUserResponseDto.add(userDto);
    });
    return allUserResponseDto;
  }

  //유저 권한 수정
  public UserRoleResponseDto modifyUserRole(Long userId, UserRoleRequestDto userRoleRequestDto) {
    User user = userRepository.getById(userId);
    user.changeRole(userRoleRequestDto.getUserRole());
    userRepository.save(user);
    return new UserRoleResponseDto(user);
  }
}
