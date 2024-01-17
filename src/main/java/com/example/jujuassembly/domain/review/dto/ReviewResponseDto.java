package com.example.jujuassembly.domain.review.dto;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLikeStatusEnum;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewResponseDto {

  private Long id;
  private UserResponseDto user;
  private String productName;
  private Long categoryId;
  private Long productId;
  private String productImage;
  private List<String> images = new ArrayList<>();
  private String description;
  private Double star;
  private String munchies;
  private Boolean isVerified;
  private Integer likeCount = 0;
  private Integer dislikeCount = 0;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;

  public ReviewResponseDto(Review savedReview) {
    this.id = savedReview.getId();
    this.user = new UserResponseDto(savedReview.getUser());
    this.productName = savedReview.getProduct().getName();
    this.categoryId = savedReview.getProduct().getCategory().getId();
    this.productId = savedReview.getProduct().getId();
    this.productImage = savedReview.getProduct().getImage();
    this.description = savedReview.getDescription();

    for (ReviewImage reviewImage : savedReview.getReviewImages()) {
      this.images.add(reviewImage.getImageUrl());
    }
    this.star = savedReview.getStar();
    this.munchies = savedReview.getMunchies();
    this.isVerified = savedReview.getIsVerified();

    for (ReviewLike reviewLike : savedReview.getReviewLikes()) {
      if (reviewLike.getStatus().equals(ReviewLikeStatusEnum.LIKE)) {
        likeCount++;
      } else {
        dislikeCount++;
      }
    }
    createdAt = savedReview.getCreatedAt();
    modifiedAt = savedReview.getModifiedAt();

  }
}
