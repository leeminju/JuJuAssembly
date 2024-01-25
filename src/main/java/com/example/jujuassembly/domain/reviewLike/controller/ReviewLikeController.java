package com.example.jujuassembly.domain.reviewLike.controller;

import com.example.jujuassembly.domain.reviewLike.dto.ReviewLikeResponseDto;
import com.example.jujuassembly.domain.reviewLike.service.ReviewLikeService;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.filter.UserDetailsImpl;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1")
public class ReviewLikeController {

  private final ReviewLikeService reviewLikeService;

  /**
   * 리뷰 추천하기
   *
   * @param categoryId  카테고리 ID
   * @param productId   제품 ID
   * @param reviewId    리뷰 ID
   * @param userDetails 인증된 사용자 정보
   * @return ApiResponse 객체
   */
  @PostMapping("/categories/{categoryId}/products/{productId}/reviews/{reviewId}/like")
  public ResponseEntity<ApiResponse> likeReview(@PathVariable Long categoryId,
      @PathVariable Long productId, @PathVariable Long reviewId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    boolean response = reviewLikeService.likeReview(categoryId, productId,
        reviewId,
        userDetails.getUser());
    if (response) {
      return ResponseEntity.ok()
          .body(new ApiResponse("리뷰를 추천했습니다.", HttpStatus.OK.value(), true));
    } else {
      return ResponseEntity.ok()
          .body(new ApiResponse("리뷰를 추천 취소했습니다.", HttpStatus.OK.value(), false));
    }
  }

  /**
   * 리뷰 추천하기
   *
   * @param categoryId  카테고리 ID
   * @param productId   제품 ID
   * @param reviewId    리뷰 ID
   * @param userDetails 인증된 사용자 정보
   * @return ApiResponse 객체
   */
  @PostMapping("/categories/{categoryId}/products/{productId}/reviews/{reviewId}/dislike")
  public ResponseEntity<ApiResponse> dislikeReview(@PathVariable Long categoryId,
      @PathVariable Long productId, @PathVariable Long reviewId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    boolean response = reviewLikeService.dislikeReview(categoryId,
        productId, reviewId, userDetails.getUser());
    if (response) {
      return ResponseEntity.ok()
          .body(new ApiResponse("리뷰를 비추천했습니다.", HttpStatus.OK.value(), true));
    } else {
      return ResponseEntity.ok()
          .body(new ApiResponse("리뷰를 비추천 취소했습니다.", HttpStatus.OK.value(), false));
    }
  }
}
