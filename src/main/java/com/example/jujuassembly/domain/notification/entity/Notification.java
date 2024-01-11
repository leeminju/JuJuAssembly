package com.example.jujuassembly.domain.notification.entity;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID 자동 생성
  private Long id;

  private String content;

  private String url;

  private boolean isRead;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id")
  private Review review;

  @Builder
  public Notification(User user, Review review, String content, String url, boolean isRead) {
    this.user = user;
    this.review = review;
    this.content = content;
    this.url = url;
    this.isRead = isRead;
  }

  public void read() {
    this.isRead = true;
  }
}
