package com.example.jujuassembly.domain.chat.service;

import com.example.jujuassembly.domain.chat.dto.LatestChatResponseDto;
import com.example.jujuassembly.domain.chat.dto.ChatRequestDto;
import com.example.jujuassembly.domain.chat.dto.ChatResponseDto;
import com.example.jujuassembly.domain.chat.entity.Chat;
import com.example.jujuassembly.domain.chat.repository.ChatRepository;
import com.example.jujuassembly.domain.room.entity.Room;
import com.example.jujuassembly.domain.room.repository.RoomRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final UserRepository userRepository;
  private final ChatRepository chatRepository;
  private final RoomRepository roomRepository;

  @Transactional
  public void save(Long roomId, ChatRequestDto chatRequestDto) {
    User sender = userRepository.findUserByIdOrElseThrow(chatRequestDto.getSenderId());
    User receiver = userRepository.findUserByIdOrElseThrow(chatRequestDto.getReceiverId());
    Room room = roomRepository.findRoomByIdOrElseThrow(roomId);
    Chat chat = new Chat(room, sender, receiver, chatRequestDto.getMessage());
    chatRepository.save(chat);
  }

  @Transactional(readOnly = true)
  public List<LatestChatResponseDto> findAllLatestChats(Long id) {
    User user = userRepository.findUserByIdOrElseThrow(id);
    List<Room> rooms = roomRepository.findByAdminIdOrUserId(id, user.getId());
    return rooms.stream()
        .map(room -> new LatestChatResponseDto(room.getPartner(id), room.getLatestChat()))
        .sorted(Comparator.comparing(LatestChatResponseDto::getCreatedAt).reversed())
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ChatResponseDto> findAllChats(Long roomId) {
    Room room = roomRepository.findRoomByIdOrElseThrow(roomId);
    List<Chat> chats = room.getChats();

    Collections.reverse(chats);

    return chats.stream()
        .map(chat -> new ChatResponseDto(chat))
        .collect(Collectors.toList());
  }
}