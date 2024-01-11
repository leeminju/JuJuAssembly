package com.example.jujuassembly.domain.reviewLike.dto;

import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLikeStatusEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLikeResponseDto {

  private Long id;
  private ReviewLikeStatusEnum status;
  private Long reviewId;
  private String userLoginId;

  public ReviewLikeResponseDto(ReviewLike reviewLike) {
    this.id = reviewLike.getId();
    this.status = reviewLike.getStatus();
    this.reviewId = reviewLike.getReview().getId();
    this.userLoginId = reviewLike.getUser().getLoginId();
  }
}
