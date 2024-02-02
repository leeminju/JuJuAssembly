package com.example.jujuassembly.domain.room.service;

import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.domain.room.dto.RoomIdResponseDto;
import com.example.jujuassembly.domain.room.dto.RoomRequestDto;
import com.example.jujuassembly.domain.room.entity.Room;
import com.example.jujuassembly.domain.room.repository.RoomRepository;
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
  public RoomIdResponseDto getOrCreate(RoomRequestDto roomRequestDto, User user) {
    Room room = roomRepository.findByAdminIdAndUserId(roomRequestDto.getAdminId(),
            roomRequestDto.getUserId())
        .orElseGet(() ->
            new Room(
                userRepository.findUserByIdOrElseThrow(roomRequestDto.getAdminId()),
                userRepository.findUserByIdOrElseThrow(roomRequestDto.getUserId())
            )
        );
    Room savedRoom = roomRepository.save(room);

//    // 알림을 받을 사용자
//    Long recipientId = user.getId().equals(roomRequestDto.getAdminId())
//        ? roomRequestDto.getUserId()
//        : roomRequestDto.getAdminId();
//    User recipient = userRepository.findUserByIdOrElseThrow(recipientId);
//
//    // 해당 유저에게 알림 전송
//    notificationService.send(recipient, "ROOM", savedRoom.getId(), user);

    return RoomIdResponseDto.builder().roomId(savedRoom.getId()).build();
  }

}