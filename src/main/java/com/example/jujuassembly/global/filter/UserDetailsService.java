package com.example.jujuassembly.global.filter;

import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsService {

  private final UserRepository userRepository;

  public UserDetailsImpl getUserDetailsImpl(String loginId) {
    User user = userRepository.findByLoginId(loginId)
        .orElseThrow(() -> new UsernameNotFoundException("Not Found" + loginId));
    return new UserDetailsImpl(user);
  }
}
