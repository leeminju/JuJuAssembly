package com.example.jujuassembly.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRequestDto {

  /**
   * 송신자 id
   */
  @NotNull
  private Long senderId;

  /**
   * 수신자 id
   */
  @NotNull
  private Long receiverId;

  /**
   * 메시지 내용
   */
  @NotBlank
  private String message;


}