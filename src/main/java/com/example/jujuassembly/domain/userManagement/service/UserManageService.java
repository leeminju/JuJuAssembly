package com.example.jujuassembly.domain.userManagement.service;

import com.example.jujuassembly.domain.user.dto.UserDetailResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.domain.userManagement.dto.UserRoleRequestDto;
import com.example.jujuassembly.domain.userManagement.dto.UserRoleResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManageService {

  private final UserRepository userRepository;


  //전체 유저 조회
  @Transactional(readOnly = true)
  public Page<UserDetailResponseDto> viewAllUsers(Pageable pageable) {
    Page<User> allUsers = userRepository.findAll(pageable);
    // "Page" 인터페이스가 제공하는 'map' 메서드 활용
    return allUsers.map(UserDetailResponseDto::new);
  }

  //유저 권한 수정
  public UserRoleResponseDto modifyUserRole(Long userId, UserRoleRequestDto userRoleRequestDto) {
    User user = userRepository.getById(userId);
    user.changeRole(userRoleRequestDto.getUserRole());
    userRepository.save(user);
    return new UserRoleResponseDto(user);
  }
}
