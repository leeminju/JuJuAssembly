package com.example.jujuassembly.domain.like.dto;

import com.example.jujuassembly.domain.like.entity.Like;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeResponseDto {

  private Long id;
  private Long productId;
  private String productName;
  private String image;
  private String description;
  private Long categoryId;
  private Integer reviewCount;
  private Double reviewAverage;
  private int likesCount;

  public LikeResponseDto(Like like) {
    this.id = like.getId();
    this.productId = like.getProduct().getId();
    this.productName = like.getProduct().getName();
    this.image = like.getProduct().getImage();
    this.description = like.getProduct().getDescription();
    this.categoryId = like.getProduct().getCategory().getId();
    this.reviewCount = like.getProduct().getReviewCount();
    this.reviewAverage = like.getProduct().getReviewAverage();
    this.likesCount = like.getProduct().getLikesCount();
  }
}
