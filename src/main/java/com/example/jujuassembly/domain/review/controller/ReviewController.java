package com.example.jujuassembly.domain.review.controller;

import com.example.jujuassembly.domain.review.dto.ReviewRequestDto;
import com.example.jujuassembly.domain.review.dto.ReviewResponseDto;
import com.example.jujuassembly.domain.review.service.ReviewService;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1")
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping("/categories/{categoryId}/products/{productId}/reviews")
  public ResponseEntity<ApiResponse<ReviewResponseDto>> createProductsReview(
      @PathVariable Long categoryId, @PathVariable Long productId,
      @RequestParam(name = "images", required = false) MultipartFile[] images,
      @Valid @RequestPart(name = "data") ReviewRequestDto requestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {

    ReviewResponseDto responseDto = reviewService.createProductsReview(categoryId, productId,
        images, requestDto, userDetails.getUser());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse("리뷰가 등록되었습니다.", HttpStatus.CREATED.value(), responseDto));
  }

  @GetMapping("/categories/{categoryId}/products/{productId}/reviews")
  public ResponseEntity<ApiResponse<Page<ReviewResponseDto>>> getProductsReview(
      @PathVariable Long categoryId, @PathVariable Long productId,
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ReviewResponseDto> reviews = reviewService.getProductsReview(categoryId, productId,
        userDetails.getUser(), pageable);
    return ResponseEntity.ok()
        .body(new ApiResponse(productId + "번 상품 내 리뷰 목록 입니다.", HttpStatus.OK.value(), reviews));
  }

  @PatchMapping("/categories/{categoryId}/products/{productId}/reviews/{reviewId}")
  public ResponseEntity<ApiResponse<ReviewResponseDto>> updateProductsReview(
      @PathVariable Long categoryId, @PathVariable Long productId, @PathVariable Long reviewId,
      @RequestParam(name = "images", required = false) MultipartFile[] images,
      @Valid @RequestPart(name = "data") ReviewRequestDto requestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {

    ReviewResponseDto responseDto = reviewService.updateProductsReview(categoryId, productId,
        reviewId, images, requestDto, userDetails.getUser());

    return ResponseEntity.ok()
        .body(new ApiResponse<>("리뷰가 수정되었습니다.", HttpStatus.OK.value(), responseDto));
  }

  @DeleteMapping("/categories/{categoryId}/products/{productId}/reviews/{reviewId}")
  public ResponseEntity<ApiResponse<ReviewResponseDto>> deleteProductsReview(
      @PathVariable Long categoryId, @PathVariable Long productId, @PathVariable Long reviewId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    reviewService.deleteProductsReview(categoryId, productId, reviewId, userDetails.getUser());

    return ResponseEntity.ok()
        .body(new ApiResponse<>(reviewId + "번 리뷰가 삭제되었습니다.", HttpStatus.OK.value()));
  }

  @GetMapping("/users/{userId}/reviews")
  public ResponseEntity<ApiResponse<Page<ReviewResponseDto>>> getMyReviews(
      @PathVariable Long userId,
      @PageableDefault(page = 1, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Page<ReviewResponseDto> reviews = reviewService.getMyReviews(userId, pageable);
    return ResponseEntity.ok()
        .body(new ApiResponse(userId + "번 사용자 리뷰 목록 입니다.", HttpStatus.OK.value(), reviews));
  }

  @Secured(UserRoleEnum.Authority.ADMIN)
  @PatchMapping("/categories/{categoryId}/products/{productId}/reviews/{reviewId}/verification")
  public ResponseEntity<ApiResponse<ReviewResponseDto>> verifyReview(@PathVariable Long categoryId,
      @PathVariable Long productId, @PathVariable Long reviewId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    ReviewResponseDto responseDto = reviewService.verifyReview(categoryId, productId, reviewId);
    return ResponseEntity.ok()
        .body(new ApiResponse(reviewId + "번　리뷰 인증 되었습니다.", HttpStatus.OK.value(), responseDto));
  }

}
