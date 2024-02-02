package com.example.jujuassembly.domain.chat.service;

import com.example.jujuassembly.domain.chat.dto.ChatRequestDto;
import com.example.jujuassembly.domain.chat.dto.ChatResponseDto;
import com.example.jujuassembly.domain.chat.entity.Chat;
import com.example.jujuassembly.domain.chat.repository.ChatRepository;
import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

  private final ChatRepository chatRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final NotificationService notificationService;

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

  public void publish(Long roomId, ChatRequestDto chatRequestDto) {
    simpMessagingTemplate.convertAndSend("/subscribe/rooms/" + roomId + "/chats", chatRequestDto);


    // 마지막으로 메시지를 보낸 채팅 조회
    Chat lastChat = chatRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId);

    // 마지막 메시지가 없거나 마지막 메시지의 발신자가 현재 메시지의 발신자와 다른 경우 알림 전송
    if (lastChat == null || !lastChat.getSenderId().equals(chatRequestDto.getSenderId())) {
      Long recipientId = chatRequestDto.getReceiverId(); // 메시지 수신자 ID
      User recipient = userRepository.findUserByIdOrElseThrow(recipientId); // 수신자 정보 조회
      User sender = userRepository.findUserByIdOrElseThrow(
          chatRequestDto.getSenderId()); // 발신자 정보 조회

      // 해당 유저(수신자)에게 채팅 알림 전송
      notificationService.send(recipient, "ROOM", roomId, sender);
    }
  }

  @Transactional
  public List<ChatResponseDto> findAllChats(Long roomId) {
    List<Chat> chats = chatRepository.findAllByRoomId(roomId);
    List<Chat> chatList = new ArrayList<>();

    for (Chat chat : chats) {
      User sender = userRepository.findUserByIdOrElseThrow(chat.getSenderId());
      chat.updateSender(sender.getImage(), sender.getNickname());
      chatList.add(chat);
    }

    return chatList.stream()
        .map(chat -> new ChatResponseDto(chat))
        .collect(Collectors.toList());
  }

}