package com.example.jujuassembly.domain.chat.controller;

import com.example.jujuassembly.domain.chat.dto.ChatRequestDto;
import com.example.jujuassembly.domain.chat.dto.ChatResponseDto;
import com.example.jujuassembly.domain.chat.service.ChatService;
import com.example.jujuassembly.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/")
public class ChatController {

  private final ChatService chatService;

  /**
   * 채팅 메시지 발행 및 저장
   *
   * @param roomId         채팅방 ID
   * @param chatRequestDto 채팅 요청 DTO
   */
  @MessageMapping("/rooms/{roomId}/chats")
  public void publish(@DestinationVariable Long roomId,
      @RequestBody @Valid ChatRequestDto chatRequestDto) {
    chatService.publish(roomId, chatRequestDto);
    chatService.save(roomId, chatRequestDto);
  }

  /**
   * 채팅방의 채팅 기록 조회
   *
   * @param roomId 채팅방 ID
   * @return 채팅방의 모든 채팅 기록과 함께 성공 응답 반환
   */
  @GetMapping("/chats")
  public ResponseEntity<ApiResponse<List<ChatResponseDto>>> findAllChats(
      @RequestParam("roomId") Long roomId) {
    List<ChatResponseDto> chatResponseDtoList = chatService.findAllChats(roomId);
    return ResponseEntity.status(HttpStatus.OK)
        .body(new ApiResponse("채팅방의 채팅 기록 조회 성공", HttpStatus.OK.value(), chatResponseDtoList));
  }

}
