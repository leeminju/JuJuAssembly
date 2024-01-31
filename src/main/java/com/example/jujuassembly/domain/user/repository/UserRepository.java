package com.example.jujuassembly.domain.user.repository;

import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.global.exception.ApiException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

public interface UserRepository extends JpaRepository<User, Long> {

  default User findUserByIdOrElseThrow(Long id){
    return findById(id).orElseThrow(
        () -> new ApiException("해당하는 사용자가 없습니다.", HttpStatus.NOT_FOUND)
    );
  }
  Optional<User> findByLoginId(String loginId);

  Optional<User> findByNickname(String nickname);

  Optional<User> findByEmail(String email);

  Optional<User> findByKakaoId(Long kakoId);

  Page<User> findAllByRole(Pageable pageable, UserRoleEnum admin);
}
