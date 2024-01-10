package com.example.jujuassembly.domain.reviewLike.entity;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "review_likes")
public class ReviewLike extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id")
  private Review review;

  @Column(nullable = false, name = "status")
  @Enumerated(value = EnumType.STRING)
  private ReviewLikeStatusEnum status;

  public ReviewLike(Review review, User user) {
    this.review = review;
    this.user = user;
  }

  public void like() {
    this.status = ReviewLikeStatusEnum.LIKE;
  }

  public void dislike() {
    this.status = ReviewLikeStatusEnum.DISLIKE;
  }
}
