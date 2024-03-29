package com.example.jujuassembly.domain.review.service;

import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.review.dto.ReviewRequestDto;
import com.example.jujuassembly.domain.review.dto.ReviewResponseDto;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.domain.reviewImage.service.ReviewImageService;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.exception.ApiException;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  private final EntityManager entityManager;

  @Transactional
  public ReviewResponseDto createProductsReview(Long categoryId, Long productId,
      MultipartFile[] images, ReviewRequestDto requestDto, User user) throws Exception {

    categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    Product product = productRepository.findProductByIdOrElseThrow(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);

    Review review = new Review(requestDto, product, user);
    Review savedReview = reviewRepository.save(review);
    // 상품의 리뷰 수 증가
    productRepository.increaseReviewCount(productId);

    if (images != null && images.length > 4) {
      throw new ApiException("사진은 4장 까지만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
    }

    if (images != null) {
      // 이미지 업로드
      reviewImageService.uploadReviewImages(savedReview, images);
    }

    return new ReviewResponseDto(savedReview);
  }

  public Page<ReviewResponseDto> getProductsReview(Long categoryId, Long productId,
      Pageable pageable) {
    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
        pageable.getSort().and(Sort.by("id")));
    categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    Product product = productRepository.findProductByIdOrElseThrow(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);

    Page<Review> reviews = reviewRepository.findAllByProduct(product, pageable);

    return reviews.map(ReviewResponseDto::new);
  }

  @Transactional
  public ReviewResponseDto updateProductsReview(Long categoryId, Long productId, Long reviewId,
      MultipartFile[] images, ReviewRequestDto requestDto) throws Exception {

    categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    Product product = productRepository.findProductByIdOrElseThrow(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.findReviewByIdOrElseThrow(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    //기존의 파일 모두 삭제
    //reviewImageService.deleteAllReviewImages(review, "reviews");
    List<ReviewImage> existingReviewImagesList = reviewImageService.findImageFromReviewImage(
        review);

    if (images == null && existingReviewImagesList.size() > 0) {
      //기존 사진만 유지
      review.update(requestDto);
      return new ReviewResponseDto(review);
    } else {
      if (images != null && existingReviewImagesList.size() + images.length > 4) {
        //4개 이상이면 사진 추가 안됌
        throw new ApiException("사진은 4장 까지만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
      }
    }
    review.update(requestDto);

    if (images != null) {
      reviewImageService.uploadReviewImages(review, images);
    }

    return new ReviewResponseDto(review);

  }


  @Transactional
  public ReviewResponseDto deleteReviewImage(Long categoryId, Long productId,
      Long reviewId, Long imageIndex) {
    Product product = productRepository.findProductByIdOrElseThrow(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.findReviewByIdOrElseThrow(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    reviewImageService.deleteReviewImage(reviewId, imageIndex);
    entityManager.detach(review);

    Review reviewAfterDelete = reviewRepository.findReviewByIdOrElseThrow(reviewId);

    return new ReviewResponseDto(reviewAfterDelete);
  }

  @Transactional
  public void deleteProductsReview(Long categoryId, Long productId, Long reviewId) {
    categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    Product product = productRepository.findProductByIdOrElseThrow(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.findReviewByIdOrElseThrow(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    // 리뷰 삭제 전에 리뷰 카운트 감소
    productRepository.decreaseReviewCount(productId);

    // 해당 리뷰에 대한 모든 알림 삭제
    notificationService.deleteNotificationByEntity("REVIEW", reviewId);

    //기존의 파일 모두 삭제
    reviewRepository.delete(review);
    reviewImageService.deleteAllReviewImages(review, "reviews");
  }

  public Page<ReviewResponseDto> getMyReviews(Long userId, Pageable pageable) {
    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
        pageable.getSort().and(Sort.by("id")));
    User user = userRepository.findUserByIdOrElseThrow(userId);

    Page<Review> reviews = reviewRepository.findAllByUser(user, pageable);
    return reviews.map(ReviewResponseDto::new);
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponseDto> getVerifiedReviews(Long userId, Pageable pageable,
      boolean isVerified) {
    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
        pageable.getSort().and(Sort.by("id")));
    User user = userRepository.findUserByIdOrElseThrow(userId);

    Page<Review> reviews;
    if (isVerified) {
      reviews = reviewRepository.findAllByUserAndIsVerified(user, true, pageable);
    } else {
      reviews = reviewRepository.findAllByUser(user, pageable);
    }
    return reviews.map(ReviewResponseDto::new);
  }

  @Transactional
  public boolean verifyReview(Long categoryId, Long productId, Long reviewId) {
    categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    Product product = productRepository.findProductByIdOrElseThrow(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.findReviewByIdOrElseThrow(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    reviewRepository.verifyReview(reviewId, !review.getIsVerified());

    if (!review.getIsVerified()) {
      return true;
    } else {
      return false;
    }
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
