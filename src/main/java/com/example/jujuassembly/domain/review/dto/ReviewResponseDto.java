package com.example.jujuassembly.domain.review.dto;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLikeStatusEnum;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ReviewResponseDto {

  private Long id;
  private UserResponseDto writer;
  private String productName;
  private List<String> images = new ArrayList<>();
  private String description;
  private Double star;
  private String munchies;
  private Boolean isVerified;
  private Integer likeCount=0;
  private Integer dislikeCount=0;

  public ReviewResponseDto(Review savedReview) {
    this.id = savedReview.getId();
    this.writer = new UserResponseDto(savedReview.getWriter());
    this.productName = savedReview.getProduct().getName();
    this.description = savedReview.getDescription();

    for (ReviewImage reviewImage : savedReview.getReviewImages()) {
      log.info(reviewImage.getImageUrl());
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

  }
}
