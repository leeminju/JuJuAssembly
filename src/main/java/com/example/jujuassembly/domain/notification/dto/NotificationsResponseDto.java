package com.example.jujuassembly.domain.notification.dto;

import java.util.List;
import lombok.Builder;

public class NotificationsResponseDto {

  /**
   * 로그인 한 유저의 모든 알림
   */
  private List<NotificationResponseDto> notificationResponses;

  /**
   * 로그인 한 유저가 읽지 않은 알림 수
   */
  private long unreadCount;

  @Builder
  public NotificationsResponseDto(List<NotificationResponseDto> notificationResponses,
      long unreadCount) {
    this.notificationResponses = notificationResponses;
    this.unreadCount = unreadCount;
  }

  public static NotificationsResponseDto of(List<NotificationResponseDto> notificationResponses,
      long count) {
    return NotificationsResponseDto.builder()
        .notificationResponses(notificationResponses)
        .unreadCount(count)
        .build();
  }
}
