package com.example.jujuassembly.domain.notification.dto;

import com.example.jujuassembly.domain.notification.entity.Notification;
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
public class NotificationResponseDto {

  private Long id;
  private String content;
  private String url;
  private LocalDateTime createdAt;
  private boolean read;


  public NotificationResponseDto(Notification notification) {
    this.id = notification.getId();
    this.content = notification.getContent();
    this.url = notification.getUrl();
    this.createdAt = notification.getCreatedAt();
    this.read = notification.isRead();
  }


}
