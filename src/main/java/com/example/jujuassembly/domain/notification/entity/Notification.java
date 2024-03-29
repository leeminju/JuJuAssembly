package com.example.jujuassembly.domain.notification.entity;

import com.example.jujuassembly.domain.notification.dto.NotificationRequestDto;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notifications")
public class Notification extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;  // 알림의 고유 식별자

  private String content;  // 알림 내용

  private String url;  // 알림과 관련된 URL

  private boolean isRead;  // 알림 읽음 여부

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;  // 알림을 받는 사용자

  private String entityType; // 알림과 관련된 엔티티의 유형
  private Long entityId;     // 알림과 관련된 엔티티의 ID

  // 알림 객체 생성 메서드
  public Notification(NotificationRequestDto requestDto) {
    this.user = requestDto.getUser();
    this.content = requestDto.getContent();
    this.url = requestDto.getUrl();
    this.isRead = requestDto.isRead();
    this.entityType = requestDto.getEntityType();
    this.entityId = requestDto.getEntityId();
  }

  // 알림 상태를 "읽음"으로 변경
  public void read() {
    this.isRead = true;
  }
}
