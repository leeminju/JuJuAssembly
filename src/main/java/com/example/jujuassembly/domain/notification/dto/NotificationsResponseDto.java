package com.example.jujuassembly.domain.notification.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationsResponseDto {

  private List<NotificationResponseDto> notificationResponses; // 로그인 한 유저의 모든 알림

  private long unreadCount; // 로그인한 유저가 읽지 않은 알림 수

  public NotificationsResponseDto(List<NotificationResponseDto> notificationResponses,
      long unreadCount) {
    this.notificationResponses = notificationResponses;
    this.unreadCount = unreadCount;
  }
}
