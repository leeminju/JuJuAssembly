package com.example.jujuassembly.domain.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.chat.entity.Chat;
import com.example.jujuassembly.domain.chat.repository.ChatRepository;
import com.example.jujuassembly.domain.chat.dto.ChatRequestDto;
import com.example.jujuassembly.domain.chat.dto.ChatResponseDto;
import com.example.jujuassembly.domain.chat.dto.LatestChatResponseDto;
import com.example.jujuassembly.domain.room.entity.Room;
import com.example.jujuassembly.domain.room.repository.RoomRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @InjectMocks
  ChatService chatService;
  @Mock
  UserRepository userRepository;
  @Mock
  ChatRepository chatRepository;
  @Mock
  RoomRepository roomRepository;

  @Test
  @DisplayName("채팅 내용 저장 테스트")
  void saveTest() {
    //given
    Long roomId = 1L;
    ChatRequestDto chatRequestDto = ChatRequestDto.builder().senderId(1L).receiverId(2L)
        .message("1").build();
    User sender = User.builder().id(chatRequestDto.getSenderId()).loginId("userId")
        .nickname("testuser1").password("1234")
        .build();
    User reciver = User.builder().id(chatRequestDto.getReceiverId()).loginId("userId2")
        .nickname("testuser2").password("1234")
        .build();
    Room room = Room.builder().id(roomId).admin(sender).user(reciver).build();

    given(userRepository.getById(chatRequestDto.getSenderId())).willReturn(sender);
    given(userRepository.getById(chatRequestDto.getReceiverId())).willReturn(reciver);
    given(roomRepository.getById(roomId)).willReturn(room);

    //when
    chatService.save(roomId, chatRequestDto);

    //then
    verify(chatRepository, times(1)).save(any(Chat.class));


  }

  @Test
  @DisplayName("최근 채팅 가져오기 테스트")
  void findAllLatestChatsTest() {
    // 가짜 사용자 데이터 생성
    Long userId = 1L;
    Long userId2 = 2L;
    User user = User.builder().id(userId).build();
    User admin = User.builder().id(userId2).build();

    //가짜 채팅목록 생성
    Chat chat1 = Chat.builder().receiver(user).content("내용").sender(admin).id(1L).build();
    Chat chat2 = Chat.builder().receiver(user).content("내용").sender(admin).id(2L).build();
    List<Chat> chats = List.of(chat1, chat2);
    // 가짜 채팅방 데이터 생성
    Room room1 = Room.builder().id(1L).chats(chats).admin(admin).user(user).build();
    Room room2 = Room.builder().id(2L).chats(chats).admin(admin).user(user).build();
    LocalDateTime now = LocalDateTime.now();
    ReflectionTestUtils.setField(chat1, Chat.class, "room", room1, Room.class);
    ReflectionTestUtils.setField(chat2, Chat.class, "room", room1, Room.class);
    ReflectionTestUtils.setField(chat1, Chat.class, "createdAt", now, LocalDateTime.class);
    ReflectionTestUtils.setField(chat2, Chat.class, "createdAt", now, LocalDateTime.class);

    // Mocking 설정
    when(userRepository.getById(userId)).thenReturn(user);
    when(roomRepository.getById(room1.getId())).thenReturn(room1);
    when(roomRepository.getById(room2.getId())).thenReturn(room2);
    when(roomRepository.findByAdminIdOrUserId(userId, user.getId())).thenReturn(
        Arrays.asList(room1, room2));

    // 테스트 대상 메서드 호출
    List<LatestChatResponseDto> result = chatService.findAllLatestChats(userId);

    // Mocking된 메서드의 호출 여부 검증
    verify(userRepository, times(1)).getById(userId);
    verify(roomRepository, times(1)).findByAdminIdOrUserId(userId, user.getId());

    // 결과 검증 - 여러 가지 방법 중에 선택
    // 1. 결과의 크기가 예상과 일치하는지 확인
    assertEquals(2, result.size());
    // 2. 특정 데이터의 일치 여부 확인 (예를 들어, room1과 room2의 비교)
    assertEquals(room2.getLatestChat().getContent(), result.get(1).getLatestMessage());
  }

//   @Test
//   @DisplayName("채팅방의 모든 채팅 가져오기 테스트")
//   void findAllChatsTest() {
//     Long userId = 1L;
//     Long userId2 = 2L;
//     User user = User.builder().id(userId).build();
//     User admin = User.builder().id(userId2).build();

//     //가짜 채팅목록 생성
//     Chat chat1 = Chat.builder().receiver(user).content("내용").sender(admin).id(1L).build();
//     Chat chat2 = Chat.builder().receiver(user).content("내용").sender(admin).id(2L).build();
//     List<Chat> chats = List.of(chat1, chat2);
//     // 가짜 채팅방 데이터 생성
//     Long roomId = 1L;
//     Room room1 = Room.builder().id(roomId).chats(chats).admin(admin).user(user).build();

//     ReflectionTestUtils.setField(chat1, Chat.class, "room", room1, Room.class);
//     ReflectionTestUtils.setField(chat2, Chat.class, "room", room1, Room.class);

//     // Mocking 설정
//     when(userRepository.getById(userId)).thenReturn(user);
//     when(roomRepository.getById(room1.getId())).thenReturn(room1);

//     // 테스트 대상 메서드 호출
//     List<ChatResponseDto> result = chatService.findAllChats(roomId);

//     // Mocking된 메서드의 호출 여부 검증
//     verify(roomRepository, times(1)).getById(roomId);

//     // 결과 검증 - 여러 가지 방법 중에 선택
//     // 1. 결과의 크기가 예상과 일치하는지 확인
//     assertEquals(2, result.size());
//     // 2. 특정 데이터의 일치 여부 확인 (예를 들어, chat1과 chat2의 비교)
//     assertEquals(chat1.getContent(), result.get(0).getMessage());
//     assertEquals(chat2.getContent(), result.get(1).getMessage());
//   }
}
