package com.example.jujuassembly.domain.room.controller;

import com.example.jujuassembly.domain.chat.dto.ChatRequestDto;
import com.example.jujuassembly.domain.chat.service.ChatService;
import com.example.jujuassembly.domain.room.dto.RoomIdResponseDto;
import com.example.jujuassembly.domain.room.dto.RoomRequestDto;
import com.example.jujuassembly.domain.room.service.RoomService;
import com.example.jujuassembly.global.filter.UserDetailsImpl;
import com.example.jujuassembly.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
@Slf4j
public class RoomController {

  private final RoomService roomService;
  private final ChatService chatService;
  private final SimpMessagingTemplate simpMessagingTemplate;

  /**
   * 채팅 메시지 발행 및 저장
   *
   * @param roomId         채팅방 ID
   * @param chatRequestDto 채팅 요청 DTO
   */
  @MessageMapping("/rooms/{roomId}")
  public void publish(@DestinationVariable Long roomId,
      @RequestBody @Valid ChatRequestDto chatRequestDto) {
    chatService.save(roomId, chatRequestDto);
    simpMessagingTemplate.convertAndSend("/subscribe/rooms/" + roomId, chatRequestDto);
  }
  /**
   * 채팅방 입장,퇴장 메시지 발행
   *
   * @param roomId         채팅방 ID
   * @param chatRequestDto 채팅 요청 DTO
   */
  @MessageMapping("/rooms/enterexit/{roomId}")
  public void enterOrExit(@DestinationVariable Long roomId,
      @RequestBody @Valid ChatRequestDto chatRequestDto) {
    simpMessagingTemplate.convertAndSend("/subscribe/rooms/" + roomId, chatRequestDto);
  }

  /**
   * 채팅방 생성 또는 조회
   *
   * @param roomRequestDto 채팅방 생성 요청 DTO
   * @return 생성 또는 조회된 채팅방 ID와 함께 성공 응답 반환
   */
  @PostMapping("/rooms") // ModelAttribute
  public ResponseEntity<ApiResponse> createOrGet(
      @RequestBody @Valid RoomRequestDto roomRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
    RoomIdResponseDto roomIdResponseDto = roomService.getOrCreate(roomRequestDto, userDetails.getUser());
    return ResponseEntity.status(HttpStatus.OK)
        .body(new ApiResponse("채팅방 생성 or 조회 완료", HttpStatus.OK.value(),
            roomIdResponseDto));
  }
}
