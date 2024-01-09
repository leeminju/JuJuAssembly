package com.example.jujuassembly.domain.reviewLike.entity;

import lombok.Getter;

@Getter
public enum ReviewLikeStatusEnum {
  DISLIKE("비추천"),
  LIKE("추천");

  private String status;

  ReviewLikeStatusEnum(String status) {
    this.status = status;
  }
}
