package com.example.jujuassembly.domain.user.repository;

import com.example.jujuassembly.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
