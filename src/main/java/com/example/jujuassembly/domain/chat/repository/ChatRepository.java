package com.example.jujuassembly.domain.chat.repository;


import com.example.jujuassembly.domain.chat.entity.Chat;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<Chat, Long> {

  List<Chat> findAllByRoomId(Long roomId);
}
