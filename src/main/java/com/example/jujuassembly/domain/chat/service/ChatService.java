package com.example.jujuassembly.domain.chat.service;

import com.example.jujuassembly.domain.chat.dto.ChatRequestDto;
import com.example.jujuassembly.domain.chat.dto.ChatResponseDto;
import com.example.jujuassembly.domain.chat.entity.Chat;
import com.example.jujuassembly.domain.chat.repository.ChatRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

  private final ChatRepository chatRepository;

  @Transactional
  public void save(Long roomId, ChatRequestDto chatRequestDto) {

    ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");

    ZonedDateTime seoulTime = ZonedDateTime.now(seoulZoneId);

    Chat chat = Chat.builder()
        .content(chatRequestDto.getMessage())
        .receiverId(chatRequestDto.getReceiverId())
        .senderId(chatRequestDto.getSenderId())
        .createdAt(seoulTime.toLocalDateTime())
        .roomId(roomId)
        .build();

    chatRepository.save(chat);
  }

  public List<ChatResponseDto> findAllChats(Long roomId) {
    List<Chat> chats = chatRepository.findAllByRoomId(roomId);

    return chats.stream()
        .map(chat -> new ChatResponseDto(chat))
        .collect(Collectors.toList());
  }

}