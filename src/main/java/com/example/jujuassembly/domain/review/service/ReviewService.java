package com.example.jujuassembly.domain.review.service;

import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.review.dto.ReviewRequestDto;
import com.example.jujuassembly.domain.review.dto.ReviewResponseDto;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.reviewImage.service.ReviewImageService;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.s3.S3Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final ReviewImageService reviewImageService;
  private final NotificationService notificationService;

  @Transactional
  public ReviewResponseDto createProductsReview(Long categoryId, Long productId,
      MultipartFile[] images, ReviewRequestDto requestDto, User user) throws Exception {
    if (images.length > 4) {
      throw new ApiException("사진은 4장 까지만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
    }

    categoryRepository.getById(categoryId);
    Product product = productRepository.getById(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);

    Review review = new Review(requestDto, product, user);
    Review savedReview = reviewRepository.save(review);
    // 이미지 업로드
    reviewImageService.uploadReviewImages(savedReview, images);

    return new ReviewResponseDto(savedReview);
  }

  public Page<ReviewResponseDto> getProductsReview(Long categoryId, Long productId,
      Pageable pageable) {
    categoryRepository.getById(categoryId);
    Product product = productRepository.getById(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);

    Page<Review> reviews = reviewRepository.findAllByProduct(product, pageable);

    return reviews.map(ReviewResponseDto::new);
  }

  @Transactional
  public ReviewResponseDto updateProductsReview(Long categoryId, Long productId, Long reviewId,
      MultipartFile[] images, ReviewRequestDto requestDto) throws Exception {
    if (images.length > 4) {
      throw new ApiException("사진은 4장 까지만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
    }
    categoryRepository.getById(categoryId);
    Product product = productRepository.getById(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.getById(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    //기존의 파일 모두 삭제
    reviewImageService.deleteAllReviewImages(review, S3Manager.REVIEW_DIRECTORY_NAME);

    //새로 업로드
    reviewImageService.uploadReviewImages(review, images);

    review.update(requestDto);

    return new ReviewResponseDto(review);
  }

  public void deleteProductsReview(Long categoryId, Long productId, Long reviewId) {
    categoryRepository.getById(categoryId);
    Product product = productRepository.getById(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.getById(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    // 해당 리뷰에 대한 모든 알림 삭제
    notificationService.deleteNotificationsByReview(review);

    //기존의 파일 모두 삭제
    reviewRepository.delete(review);
    reviewImageService.deleteAllReviewImages(review, S3Manager.REVIEW_DIRECTORY_NAME);
  }

  public Page<ReviewResponseDto> getMyReviews(Long userId, Pageable pageable) {
    User user = userRepository.getById(userId);

    Page<Review> reviews = reviewRepository.findAllByUser(user, pageable);
    return reviews.map(ReviewResponseDto::new);
  }

  @Transactional
  public ReviewResponseDto verifyReview(Long categoryId, Long productId, Long reviewId) {
    categoryRepository.getById(categoryId);
    Product product = productRepository.getById(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.getById(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    if (review.getIsVerified()) {
      throw new ApiException("이미 인증된 리뷰 입니다.", HttpStatus.BAD_REQUEST);
    }
    review.verify();

    return new ReviewResponseDto(review);
  }


  //상품 카테고리 일치 검증
  private void checkProductCategoryAndCategoryIdEquality(Product product, Long categoryId) {
    if (!product.getCategory().getId().equals(categoryId)) {
      throw new ApiException("해당 카테고리 상품이 아닙니다.", HttpStatus.BAD_REQUEST);
    }
  }

  //리뷰와 상품 일치 검증
  private void checkReviewProductAndProductIdEquality(Review review, Long productId) {
    if (!review.getProduct().getId().equals(productId)) {
      throw new ApiException("해당 상품의 리뷰가 아닙니다.", HttpStatus.BAD_REQUEST);
    }
  }


}
