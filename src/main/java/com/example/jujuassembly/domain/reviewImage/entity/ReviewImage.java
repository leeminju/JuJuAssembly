package com.example.jujuassembly.domain.reviewImage.entity;

import com.example.jujuassembly.domain.review.entity.Review;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "review_images")
public class ReviewImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id")
  private Review review;

  @Column(name = "image_url")
  private String imageUrl;

  public ReviewImage(Review review, String imageUrl) {
    this.review = review;
    this.imageUrl = imageUrl;
    review.getReviewImages().add(this);
  }
}
