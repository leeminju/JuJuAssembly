package com.example.jujuassembly.domain.room.controller;

import com.example.jujuassembly.domain.chat.dto.ChatRequestDto;
import com.example.jujuassembly.domain.chat.service.ChatService;
import com.example.jujuassembly.domain.room.dto.RoomIdResponseDto;
import com.example.jujuassembly.domain.room.dto.RoomRequestDto;
import com.example.jujuassembly.domain.room.service.RoomService;
import com.example.jujuassembly.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;
  private final ChatService chatService;
  private final SimpMessagingTemplate simpMessagingTemplate;

  //변경해줘야 할 사항 POst매핑을 메세지매핑으로 ->@pathvariable에서 Destinationvariable로
  @MessageMapping("/rooms/{roomId}")
  public void publish(@DestinationVariable Long roomId,
      @RequestBody @Valid ChatRequestDto chatRequestDto) {
    chatService.save(roomId, chatRequestDto);
    simpMessagingTemplate.convertAndSend("/subscribe/rooms/" + roomId, chatRequestDto.getMessage());
  }

  @GetMapping("/rooms")//ModelAttribute
  public ResponseEntity<ApiResponse> createOrGet(
      @RequestBody @Valid RoomRequestDto roomRequestDto) {
    RoomIdResponseDto roomIdResponseDto = roomService.getOrCreate(roomRequestDto);
    return ResponseEntity.status(HttpStatus.OK)
        .body(new ApiResponse("채팅방 생성or조회 완료", HttpStatus.OK.value(),
            roomIdResponseDto));
  }
}
