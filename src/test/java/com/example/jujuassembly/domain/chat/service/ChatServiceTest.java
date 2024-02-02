package com.example.jujuassembly.domain.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.chat.dto.ChatRequestDto;
import com.example.jujuassembly.domain.chat.dto.ChatResponseDto;
import com.example.jujuassembly.domain.chat.entity.Chat;
import com.example.jujuassembly.domain.chat.repository.ChatRepository;
import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @InjectMocks
  ChatService chatService;
  @Mock
  ChatRepository chatRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  private SimpMessagingTemplate simpMessagingTemplate;
  @Mock
  private NotificationService notificationService;
  @Test
  @DisplayName("채팅 내용 저장 테스트")
  void saveTest() {
    // Mock data
    Long roomId = 1L;
    ChatRequestDto chatRequestDto = ChatRequestDto.builder().message("hello").receiverId(1L)
        .senderId(2L).build();

    // Set a fixed ZonedDateTime for testing
    ZonedDateTime fixedTime = ZonedDateTime.of(2024, 1, 30, 12, 0, 0, 0, ZoneId.of("Asia/Seoul"));

    // Mock ZonedDateTime.now() to return the fixed time
    try (MockedStatic<ZonedDateTime> mockedStatic = mockStatic(ZonedDateTime.class)) {
      mockedStatic.when(() -> ZonedDateTime.now(ZoneId.of("Asia/Seoul"))).thenReturn(fixedTime);

      // Call the actual method
      chatService.save(roomId, chatRequestDto);

      // Verify that save method in chatRepository is called with any Chat object
      verify(chatRepository).save(any(Chat.class));
    }

  }

  @Test
  @DisplayName("채팅방의 모든 채팅 가져오기 테스트")
  void findAllChatsTest() {
    // 테스트용 가짜 데이터
    Long roomId = 1L;
    Long senderId = 2L;
    User sender = User.builder().role(UserRoleEnum.USER).id(senderId).nickname("testuser").image("image_url").build();

    // 샘플 Chat 객체들 생성
    Chat chat1 = Chat.builder().content("안녕하세요").roomId(roomId).receiverId(2L).senderId(2L).build();
    Chat chat2 = Chat.builder().content("어떻게 지내세요").roomId(roomId).receiverId(3L).senderId(2L)
        .build();
    List<Chat> fakeChats = new ArrayList<>();
    fakeChats.add(chat1);
    fakeChats.add(chat2);

    //메서드의 동작을 mock으로 설정
    when(chatRepository.findAllByRoomId(roomId)).thenReturn(fakeChats);
    when(userRepository.findUserByIdOrElseThrow(senderId)).thenReturn(sender);

    // 실제 메서드 호출
    List<ChatResponseDto> result = chatService.findAllChats(roomId);

    // 메서드가 주어진 roomId로 호출되었는지 확인
    verify(chatRepository).findAllByRoomId(roomId);

    // 결과 확인
    assertEquals(2, result.size());

    ChatResponseDto expect1 = result.get(0);
    assertEquals("안녕하세요", expect1.getMessage());
    assertEquals(2L, expect1.getSenderId());

    ChatResponseDto expect2 = result.get(1);
    assertEquals("어떻게 지내세요", expect2.getMessage());
    assertEquals(2L, expect2.getSenderId());

  }
  @Test
  @DisplayName("채팅 전송 테스트")
  void testPublish() {
    // Given
    Long roomId = 1L;
    ChatRequestDto chatRequestDto = ChatRequestDto.builder().senderId(2L).receiverId(1L).build();
    User receiver = User.builder().id(2L).build();
    User sender = User.builder().id(3L).build();
    Chat chat = Chat.builder().roomId(roomId).content("hi").senderId(3L).receiverId(2L).build();

    // Mocking
    when(chatRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId)).thenReturn(chat);
    when(userRepository.findUserByIdOrElseThrow(eq(2L))).thenReturn(receiver);
    when(userRepository.findUserByIdOrElseThrow(eq(3L))).thenReturn(sender);

    // When
    chatService.publish(roomId, chatRequestDto);

    // Then
    verify(chatRepository, times(1)).findFirstByRoomIdOrderByCreatedAtDesc(roomId);
    verify(userRepository, times(2)).findUserByIdOrElseThrow(anyLong());
    verify(notificationService, times(1)).send(any(), anyString(), anyLong(), any());

    // Verify WebSocket interaction
    String expectedDestination = "/subscribe/rooms/" + roomId + "/chats";
    verify(simpMessagingTemplate).convertAndSend(expectedDestination, chatRequestDto);
  }
}
