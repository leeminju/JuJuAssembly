package com.example.jujuassembly.domain.review.controller;

import com.example.jujuassembly.domain.review.dto.ReviewRequestDto;
import com.example.jujuassembly.domain.review.dto.ReviewResponseDto;
import com.example.jujuassembly.domain.review.service.ReviewService;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum.Authority;
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


  /**
   * 리뷰 작성 API
   *
   * @param categoryId  작성할 상품의 카테고리 ID
   * @param productId   작성할 상품의 ID
   * @param images      리뷰에 첨부되는 이미지 파일들 (선택적)
   * @param requestDto  리뷰 정보를 담고 있는 객체
   * @param userDetails 현재 인증된 사용자의 상세 정보를 담고 있는 객체
   * @return 생성된 리뷰 정보와 함께 상태 코드 201(CREATED)로 응답
   * @throws Exception 예외 처리
   */
  @PostMapping("/categories/{categoryId}/products/{productId}/reviews")
  public ResponseEntity<ApiResponse<ReviewResponseDto>> createProductsReview(
      @PathVariable Long categoryId, @PathVariable Long productId,
      @RequestParam MultipartFile[] images,
      @Valid @RequestPart(name = "data") ReviewRequestDto requestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {

    ReviewResponseDto responseDto = reviewService.createProductsReview(categoryId, productId,
        images, requestDto, userDetails.getUser());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse("리뷰가 등록되었습니다.", HttpStatus.CREATED.value(), responseDto));
  }

  /**
   * 상품 리뷰 목록 조회 API
   *
   * @param categoryId  조회할 상품이 속한 카테고리의 식별자
   * @param productId   조회할 상품의 식별자
   * @param pageable    페이지네이션 정보 (기본값: 페이지 크기 10, 생성일 기준 내림차순 정렬)
   * @return 상태 코드 200(OK)와 함께 상품의 리뷰 목록을 응답
   */
  @GetMapping("/categories/{categoryId}/products/{productId}/reviews")
  public ResponseEntity<ApiResponse<Page<ReviewResponseDto>>> getProductsReview(
      @PathVariable Long categoryId, @PathVariable Long productId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ReviewResponseDto> reviews = reviewService.getProductsReview(categoryId, productId, pageable);
    return ResponseEntity.ok()
        .body(new ApiResponse(productId + "번 상품 내 리뷰 목록 입니다.", HttpStatus.OK.value(), reviews));
  }

  /**
   * 상품 리뷰 수정 API
   *
   * @param categoryId  수정할 상품이 속한 카테고리의 식별자
   * @param productId   수정할 상품의 식별자
   * @param reviewId    수정할 리뷰의 식별자
   * @param images      변경된 리뷰 이미지 파일 배열 (선택적)
   * @param requestDto  변경할 리뷰 정보를 담고 있는 객체
   * @return 상태 코드 200(OK)와 함께 수정된 리뷰 정보를 응답
   * @throws Exception 예외 발생 시 처리
   */

  @PatchMapping("/categories/{categoryId}/products/{productId}/reviews/{reviewId}")
  public ResponseEntity<ApiResponse<ReviewResponseDto>> updateProductsReview(
      @PathVariable Long categoryId, @PathVariable Long productId, @PathVariable Long reviewId,
      @RequestParam MultipartFile[] images,
      @Valid @RequestPart(name = "data") ReviewRequestDto requestDto) throws Exception {

    ReviewResponseDto responseDto = reviewService.updateProductsReview(categoryId, productId,
        reviewId, images, requestDto);

    return ResponseEntity.ok()
        .body(new ApiResponse<>("리뷰가 수정되었습니다.", HttpStatus.OK.value(), responseDto));
  }

  /**
   * 상품 리뷰 삭제 API
   *
   * @param categoryId  카테고리 ID
   * @param productId   상품 ID
   * @param reviewId    리뷰 ID
   * @return ApiResponse 객체를 ResponseEntity로 감싼 형태
   */
  @DeleteMapping("/categories/{categoryId}/products/{productId}/reviews/{reviewId}")
  public ResponseEntity<ApiResponse<ReviewResponseDto>> deleteProductsReview(
      @PathVariable Long categoryId, @PathVariable Long productId, @PathVariable Long reviewId) {

    reviewService.deleteProductsReview(categoryId, productId, reviewId);

    return ResponseEntity.ok()
        .body(new ApiResponse<>(reviewId + "번 리뷰가 삭제되었습니다.", HttpStatus.OK.value()));
  }

  /**
   * 특정 사용자의 리뷰 목록을 가져오는 API
   *
   * @param userId   사용자 ID
   * @param pageable 페이지 정보 (기본값: 1페이지, 10개씩, 생성일 기준 내림차순 정렬)
   * @return ApiResponse 객체를 ResponseEntity로 감싼 형태
   */
  @GetMapping("/users/{userId}/reviews")
  public ResponseEntity<ApiResponse<Page<ReviewResponseDto>>> getMyReviews(
      @PathVariable Long userId,
      Pageable pageable
  ) {
    Page<ReviewResponseDto> reviews = reviewService.getMyReviews(userId, pageable);
    return ResponseEntity.ok()
        .body(new ApiResponse(userId + "번 사용자 리뷰 목록 입니다.", HttpStatus.OK.value(), reviews));
  }

  @Secured(Authority.ADMIN)
  @PatchMapping("/categories/{categoryId}/products/{productId}/reviews/{reviewId}/verification")
  public ResponseEntity<ApiResponse<ReviewResponseDto>> verifyReview(@PathVariable Long categoryId,
      @PathVariable Long productId, @PathVariable Long reviewId) {
    ReviewResponseDto responseDto = reviewService.verifyReview(categoryId, productId, reviewId);
    return ResponseEntity.ok()
        .body(new ApiResponse(reviewId + "번　리뷰 인증 되었습니다.", HttpStatus.OK.value(), responseDto));
  }

}
