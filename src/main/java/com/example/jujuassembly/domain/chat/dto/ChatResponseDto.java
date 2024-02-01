package com.example.jujuassembly.domain.chat.dto;

import com.example.jujuassembly.domain.chat.entity.Chat;
import com.example.jujuassembly.domain.user.entity.User;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatResponseDto {

  /**
   * 송신자 아이디
   */
  private Long senderId;

  /**
   * 송신자 닉네임
   */
  private String senderNickname;

  /**
   * 송신자 프로필 이미지
   */
  private String senderImage;

  /**
   * 메시지 내용
   */
  private String message;

  /**
   * 메시지 생성 시간
   */
  private LocalDateTime createdAt;

  public ChatResponseDto(Chat chat) {
    this.senderId = chat.getSenderId();
    this.senderNickname = chat.getSenderNickname();
    this.senderImage = chat.getSenderImage();
    this.message = chat.getContent();
    this.createdAt = chat.getCreatedAt();
  }

}
