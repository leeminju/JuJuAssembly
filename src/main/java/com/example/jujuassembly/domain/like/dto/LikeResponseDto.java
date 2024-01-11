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
  private String product;
  private String user;

  public LikeResponseDto(Like like) {
    this.id = like.getId();
    this.product = like.getProduct().getName();
    this.user = like.getUser().getNickname();
  }
}
