package com.example.jujuassembly.domain.reviewLike.service;

import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.notification.entity.Notification;
import com.example.jujuassembly.domain.notification.repository.NotificationRepository;
import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.reviewLike.dto.ReviewLikeResponseDto;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLikeStatusEnum;
import com.example.jujuassembly.domain.reviewLike.repository.ReviewLikeRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.exception.ApiException;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final ReviewRepository reviewRepository;
  private final ReviewLikeRepository reviewLikeRepository;

  private final NotificationService notificationService;
  private final NotificationRepository notificationRepository;

  @Transactional
  public Optional<ReviewLikeResponseDto> likeReview(Long categoryId, Long productId, Long reviewId,
      User user) {
    ReviewLikeResponseDto responseDto = null;
    categoryRepository.getById(categoryId);
    Product product = productRepository.getById(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.getById(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    Optional<ReviewLike> reviewLike = reviewLikeRepository.findByReviewAndUser(review, user);

    boolean isNewLike = false;

    if (reviewLike.isPresent()) {
      if (reviewLike.get().getStatus().equals(ReviewLikeStatusEnum.LIKE)) {
        reviewLikeRepository.delete(reviewLike.get());//이미 추천을 눌렀다면 추천 해제
        // 데이터베이스 알림 삭제
        notificationService.deleteNotificationByEntity("REVIEW", reviewId);
        return Optional.empty();
      } else {
        reviewLike.get().like();//비추천 -> 추천으로 변경
        responseDto = new ReviewLikeResponseDto(reviewLike.get());
        isNewLike = true;
      }
    } else {
      ReviewLike newReviewLike = new ReviewLike(review, user);
      newReviewLike.like();
      ReviewLike savedReviewLike = reviewLikeRepository.save(newReviewLike);
      responseDto = new ReviewLikeResponseDto(savedReviewLike);
      isNewLike = true;  // 새로운 '좋아요'로 설정
    }

    if (isNewLike && !user.equals(review.getUser())) {
      // 새로운 '좋아요'가 있고, 리뷰 작성자와 다른 사용자가 '좋아요'를 눌렀을 경우에만 알림 발송
      Notification notification = notificationService.createNotification(review.getUser(), "REVIEW",
          reviewId, user);
      notificationRepository.save(notification);
    }

    return Optional.of(responseDto);
  }


  @Transactional
  public Optional<ReviewLikeResponseDto> dislikeReview(Long categoryId, Long productId,
      Long reviewId, User user) {
    ReviewLikeResponseDto responseDto = null;
    categoryRepository.getById(categoryId);
    Product product = productRepository.getById(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.getById(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    Optional<ReviewLike> reviewLike = reviewLikeRepository.findByReviewAndUser(review, user);

    if (reviewLike.isPresent()) {
      if (reviewLike.get().getStatus().equals(ReviewLikeStatusEnum.DISLIKE)) {
        reviewLikeRepository.delete(reviewLike.get());//이미 비추천을 눌렀다면 추천 해제
        return Optional.empty();
      } else {
        reviewLike.get().dislike();//추천 -> 비추천으로 변경
        responseDto = new ReviewLikeResponseDto(reviewLike.get());
      }
    } else {
      ReviewLike newReviewLike = new ReviewLike(review, user);
      newReviewLike.dislike();
      ReviewLike savedReviewLike = reviewLikeRepository.save(newReviewLike);
      responseDto = new ReviewLikeResponseDto(savedReviewLike);
    }

    return Optional.of(responseDto);
  }


  //상품 카테고리 일치 검증
  private void checkProductCategoryAndCategoryIdEquality(Product product, Long categoryId) {
    if (!Objects.equals(product.getCategory().getId(), categoryId)) {
      throw new ApiException("해당 카테고리 상품이 아닙니다.", HttpStatus.BAD_REQUEST);
    }
  }

  //리뷰와 상품 일치 검증
  private void checkReviewProductAndProductIdEquality(Review review, Long productId) {
    if (!Objects.equals(review.getProduct().getId(), productId)) {
      throw new ApiException("해당 상품의 리뷰가 아닙니다.", HttpStatus.BAD_REQUEST);
    }
  }


}
