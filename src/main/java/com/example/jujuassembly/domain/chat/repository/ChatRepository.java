package com.example.jujuassembly.domain.chat.repository;


import com.example.jujuassembly.domain.chat.entity.Chat;
import com.example.jujuassembly.global.exception.ApiException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

public interface ChatRepository extends JpaRepository<Chat, Long> {

  default Chat findChatByIdOrElseThrow(Long id) {
    return findById(id).orElseThrow(
        () -> new ApiException("해당 채팅을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    );
  }

}
