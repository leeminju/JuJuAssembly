package com.example.jujuassembly.domain.chat.repository;


import com.example.jujuassembly.domain.chat.entity.Chat;
import com.example.jujuassembly.global.exception.ApiException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
