package com.example.jujuassembly.domain.room.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.room.dto.RoomIdResponseDto;
import com.example.jujuassembly.domain.room.dto.RoomRequestDto;
import com.example.jujuassembly.domain.room.entity.Room;
import com.example.jujuassembly.domain.room.repository.RoomRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.annotation.Transactional;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

  @InjectMocks
  RoomService roomService;
  @Mock
  UserRepository userRepository;
  @Mock
  RoomRepository roomRepository;


  @Test
  @Transactional
  @DisplayName("채팅방 생성 or 조회 테스트")
  void testGetOrCreate() {
    // 가짜 사용자 데이터 생성
    Long adminId = 1L;
    Long userId = 2L;
    User adminUser = User.builder().id(adminId).build();
    User normalUser = User.builder().id(userId).build();

    // 가짜 채팅방 데이터 생성
    Room fakeRoom = new Room(adminUser, normalUser);

    // Mocking 설정
    when(userRepository.getById(adminId)).thenReturn(adminUser);
    when(userRepository.getById(userId)).thenReturn(normalUser);
    when(roomRepository.findByAdminIdAndUserId(adminId, userId)).thenReturn(Optional.of(fakeRoom));
    when(roomRepository.save(any(Room.class))).thenReturn(fakeRoom);

    // 테스트 대상 메서드 호출
    RoomRequestDto roomRequestDto = RoomRequestDto.builder().adminId(adminId).userId(userId)
        .build();
    RoomIdResponseDto result = roomService.getOrCreate(roomRequestDto);

    // Mocking된 메서드의 호출 여부 검증
    verify(roomRepository, times(1)).findByAdminIdAndUserId(adminId, userId);
    verify(roomRepository, times(1)).save(any(Room.class));

    // 결과 검증
    assertEquals(fakeRoom.getId(), result.getRoomId());
  }
}
