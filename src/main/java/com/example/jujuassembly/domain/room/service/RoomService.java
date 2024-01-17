package com.example.jujuassembly.domain.room.service;

import com.example.jujuassembly.domain.room.repository.RoomRepository;
import com.example.jujuassembly.domain.room.dto.RoomIdResponseDto;
import com.example.jujuassembly.domain.room.dto.RoomRequestDto;
import com.example.jujuassembly.domain.room.entity.Room;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

  private final UserRepository userRepository;
  private final RoomRepository roomRepository;


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
    return RoomIdResponseDto.builder().roomId(savedRoom.getId()).build();
  }

}