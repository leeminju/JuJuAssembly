package com.example.jujuassembly.domain.room.controller;

import com.example.jujuassembly.domain.chat.dto.ChatRequestDto;
import com.example.jujuassembly.domain.room.dto.RoomIdResponseDto;
import com.example.jujuassembly.domain.room.dto.RoomRequestDto;
import com.example.jujuassembly.domain.room.service.RoomService;
import com.example.jujuassembly.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class RoomController {

  private final RoomService roomService;
  private final SimpMessagingTemplate simpMessagingTemplate;
  public ConcurrentHashMap<Long, List<String>> connectedUser = new ConcurrentHashMap<>();

  /**
   * 채팅방 입장 및 퇴장 메시지 처리.
   *
   * @param roomId         채팅방 ID
   * @param chatRequestDto 채팅 요청 DTO
   * @param accessor       WebSocket 세션 정보를 담고 있는 SimpMessageHeaderAccessor
   */
  @MessageMapping("/rooms/enterexit/{roomId}")
  public void enterOrExit(@DestinationVariable Long roomId,
      @RequestBody @Valid ChatRequestDto chatRequestDto, SimpMessageHeaderAccessor accessor) {
    // 채팅 요청을 지정된 채팅방에 브로드캐스트
    simpMessagingTemplate.convertAndSend("/subscribe/rooms/" + roomId + "/chats", chatRequestDto);

    // 지정된 채팅방의 연결된 사용자 목록 업데이트
    if (connectedUser != null && connectedUser.containsKey(roomId)
        && connectedUser.get(roomId) != null) {
      List<String> userList = connectedUser.get(roomId);

      // 사용자가 이미 목록에 있는지 확인하고 해당에 따라 업데이트
      if (userList.contains(accessor.getSessionId())) {
        userList.remove(accessor.getSessionId()); // 사용자가 채팅방을 나가는 경우
      } else {
        userList.add(accessor.getSessionId());    // 사용자가 채팅방에 참여하는 경우
      }
    } else {
      // 채팅방이 맵에 없는 경우, 사용자를 포함한 새로운 목록을 만들고 맵에 추가
      connectedUser.putIfAbsent(roomId, new ArrayList<>(List.of(accessor.getSessionId())));
    }
  }

  /**
   * 채팅방 생성 또는 조회
   *
   * @param roomRequestDto 채팅방 생성 요청 DTO
   * @return 생성 또는 조회된 채팅방 ID와 함께 성공 응답 반환
   */
  @PostMapping("/rooms") // ModelAttribute
  public ResponseEntity<ApiResponse> createOrGet(
      @RequestBody @Valid RoomRequestDto roomRequestDto) {
    RoomIdResponseDto roomIdResponseDto = roomService.getOrCreate(roomRequestDto
    );
    return ResponseEntity.status(HttpStatus.OK)
        .body(new ApiResponse("채팅방 생성 or 조회 완료", HttpStatus.OK.value(),
            roomIdResponseDto));
  }
}
