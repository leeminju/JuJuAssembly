package com.example.jujuassembly.domain.reviewImage.entity;

import com.example.jujuassembly.domain.review.entity.Review;
import jakarta.persistence.*;
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
@Table(name = "review_images")
public class ReviewImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;// 리뷰 이미지 ID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;// 이미지가 저장된 리뷰 정보

  @Column(name = "image_url")
  private String imageUrl;// 이미지 주소

  //ReviewImage 생성자입니다.
  public ReviewImage(Review review, String imageUrl) {
    this.review = review;
    this.imageUrl = imageUrl;
    review.getReviewImages().add(this);
  }
}
