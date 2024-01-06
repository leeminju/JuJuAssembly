package com.example.jujuassembly.domain.user.service;

import com.example.jujuassembly.domain.user.dto.SingupRequestDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void signup(SingupRequestDto singupRequestDto) {

    String loginId = singupRequestDto.getLoginId();
    String nickname = singupRequestDto.getNickname();
    String email = singupRequestDto.getEmail();

    String password = singupRequestDto.getPassword();
    String passwordCheck = singupRequestDto.getPasswordCheck();

    Long firstPreferredCategoryId = singupRequestDto.getFirstPreferredCategoryId();
    Long secondPreferredCategoryId = singupRequestDto.getSecondPreferredCategoryId();

    // id, nickname, email 중복 검증
    if (!userRepository.findByLoginId(loginId).isEmpty()) {
      throw new IllegalArgumentException("중복된 loginId 입니다.");
    }
    if (!userRepository.findByNickname(nickname).isEmpty()) {
      throw new IllegalArgumentException("중복된 nickname 입니다.");
    }
    if (!userRepository.findByEmail(email).isEmpty()) {
      throw new IllegalArgumentException("중복된 email 입니다.");
    }

    // categoryId 검증 추가///////////////

    // password 확인
    // 1. nickname과 같은 값이 포함됐는지
    if (password.contains(nickname)) {
      throw new IllegalArgumentException("비밀번호에 닉네임과 같은 값이 포함될 수 없습니다.");
    }
    // 2. 비밀번호 확인이 비밀번호와 일치하는지
    if (!password.equals(passwordCheck)) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    User user = new User(loginId, nickname, email, passwordEncoder.encode(password), null, null);
    userRepository.save(user);
  }
}
