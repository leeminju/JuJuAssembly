package com.example.jujuassembly.domain.notification.dto;

import com.example.jujuassembly.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationRequestDto {

  private User user;           // 알림을 받는 사용자
  private String entityType;   // 알림과 관련된 엔티티 유형
  private Long entityId;       // 알림과 관련된 엔티티 ID
  private String content;      // 알림 내용
  private String url;          // 알림과 관련된 URL
  private boolean isRead;      // 알림 읽음 여부

}
