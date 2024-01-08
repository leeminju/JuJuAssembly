package com.example.jujuassembly.domain.review.controller;

import com.example.jujuassembly.domain.review.dto.ReviewRequestDto;
import com.example.jujuassembly.domain.review.dto.ReviewResponseDto;
import com.example.jujuassembly.domain.review.service.ReviewService;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/categories")
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping("/{categoryId}/products/{productId}/reviews")
  public ResponseEntity<ApiResponse<ReviewResponseDto>> createProductsReview(
      @PathVariable Long categoryId,
      @PathVariable Long productId,
      @RequestParam(name = "images", required = false) MultipartFile[] images,
      @RequestPart(name = "data") ReviewRequestDto requestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    ReviewResponseDto responseDto = reviewService.createProductsReview(categoryId, productId, images, requestDto, userDetails.getUser());

    return new ResponseEntity<>(
        new ApiResponse<>("리뷰가 등록되었습니다", HttpStatus.CREATED.value(), responseDto),
        HttpStatus.CREATED);
  }
}
