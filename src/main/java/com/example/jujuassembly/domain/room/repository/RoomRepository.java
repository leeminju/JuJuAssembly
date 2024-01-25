package com.example.jujuassembly.domain.room.repository;

import com.example.jujuassembly.domain.room.entity.Room;
import com.example.jujuassembly.global.exception.ApiException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

public interface RoomRepository extends JpaRepository<Room, Long> {

  default Room findRoomByIdOrElseThrow(Long id) {
    return findById(id).orElseThrow(
        () -> new ApiException("해당하는 채팅방이 없습니다.", HttpStatus.NOT_FOUND)
    );
  }

  Optional<Room> findByAdminIdAndUserId(Long adminId, Long userId);

  List<Room> findByAdminIdOrUserId(Long adminId, Long userId);
}
