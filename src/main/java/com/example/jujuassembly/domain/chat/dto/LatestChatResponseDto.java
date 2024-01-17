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
public class LatestChatResponseDto {

  /**
   * 채팅방 id
   */
  private Long roomId;

  /**
   * 채팅 상대방 id
   */
  private Long partnerId;

  /**
   * 채팅 상대방 이름
   */
  private String partnerNickname;

  /**
   * 채팅 상대방 이미지 url
   */
  private String partnerImage;

  /**
   * 가장 최근 메시지 내용
   */
  private String latestMessage;

  /**
   * 가장 최근 메시지의 생성 시간
   */
  private LocalDateTime createdAt;

  public LatestChatResponseDto(User partner, Chat chat) {
    this.roomId = chat.getRoom().getId();
    this.partnerId = partner.getId();
    this.partnerNickname = partner.getNickname();
    this.partnerImage = partner.getImage();
    this.latestMessage = chat.getContent();
    this.createdAt = chat.getCreatedAt();
  }

}