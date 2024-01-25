package com.example.jujuassembly.domain.room.service;

import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.domain.room.repository.RoomRepository;
import com.example.jujuassembly.domain.room.dto.RoomIdResponseDto;
import com.example.jujuassembly.domain.room.dto.RoomRequestDto;
import com.example.jujuassembly.domain.room.entity.Room;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final NotificationService notificationService;


  @Transactional
  public RoomIdResponseDto getOrCreate(RoomRequestDto roomRequestDto) {
    Room room = roomRepository.findByAdminIdAndUserId(roomRequestDto.getAdminId(),
            roomRequestDto.getUserId())
        .orElseGet(() ->
            new Room(
                userRepository.getById(roomRequestDto.getAdminId()),
                userRepository.getById(roomRequestDto.getUserId())
            )
        );
    Room savedRoom = roomRepository.save(room);

    // 채팅을 시작하는 사용자 정보 조회
    User actionUser = userRepository.getById(roomRequestDto.getUserId());

    // 관리자에게 채팅방 생성 알림 보내기
    if (savedRoom.getAdmin().getId().equals(roomRequestDto.getAdminId())) {
      notificationService.send(savedRoom.getAdmin(), "ROOM", savedRoom.getId(), actionUser);
    }

    return RoomIdResponseDto.builder().roomId(savedRoom.getId()).build();
  }

}