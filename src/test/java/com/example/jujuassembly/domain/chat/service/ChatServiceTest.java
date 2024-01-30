package com.example.jujuassembly.domain.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.chat.dto.ChatRequestDto;
import com.example.jujuassembly.domain.chat.dto.ChatResponseDto;
import com.example.jujuassembly.domain.chat.entity.Chat;
import com.example.jujuassembly.domain.chat.repository.ChatRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @InjectMocks
  ChatService chatService;
  @Mock
  ChatRepository chatRepository;

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

    // 샘플 Chat 객체들 생성
    Chat chat1 = Chat.builder().content("안녕하세요").roomId(roomId).receiverId(2L).senderId(3L).build();
    Chat chat2 = Chat.builder().content("어떻게 지내세요").roomId(roomId).receiverId(3L).senderId(2L)
        .build();

    //메서드의 동작을 mock으로 설정
    when(chatRepository.findAllByRoomId(roomId)).thenReturn(Arrays.asList(chat1, chat2));

    // 실제 메서드 호출
    List<ChatResponseDto> chatResponseDtoList = chatService.findAllChats(roomId);

    // 메서드가 주어진 roomId로 호출되었는지 확인
    verify(chatRepository).findAllByRoomId(roomId);

    // 결과 확인
    assertEquals(2, chatResponseDtoList.size());

    ChatResponseDto responseDto1 = chatResponseDtoList.get(0);
    assertEquals("안녕하세요", responseDto1.getMessage());
    assertEquals(3L, responseDto1.getSenderId());
    assertEquals(2L, responseDto1.getReceiverId());

    ChatResponseDto responseDto2 = chatResponseDtoList.get(1);
    assertEquals("어떻게 지내세요", responseDto2.getMessage());
    assertEquals(2L, responseDto2.getSenderId());
    assertEquals(3L, responseDto2.getReceiverId());

  }
}
