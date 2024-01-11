package com.example.jujuassembly.domain.user.repository;

import com.example.jujuassembly.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByLoginId(String loginId);

  Optional<User> findByNickname(String nickname);

  Optional<User> findByEmail(String email);

  Optional<User> findByKakaoId(Long kakoId);
}
