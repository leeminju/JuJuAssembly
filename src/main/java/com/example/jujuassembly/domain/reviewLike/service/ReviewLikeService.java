package com.example.jujuassembly.domain.reviewLike.service;

import com.example.jujuassembly.domain.category.repository.CategoryRepository;
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

  private static boolean result = false;

  @Transactional
  public boolean likeReview(Long categoryId, Long productId, Long reviewId,
      User user) {

    categoryRepository.existsById(categoryId);
    Product product = productRepository.findProductByIdOrElseThrow(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.findReviewByIdOrElseThrow(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    if (user.getId().equals(review.getUser().getId())) {
      throw new ApiException("본인의 리뷰에 추천 누를 수 없습니다.", HttpStatus.BAD_REQUEST);
    }

    Optional<ReviewLike> reviewResponse = reviewLikeRepository.findByReviewAndUser(review, user);

    reviewResponse.ifPresentOrElse(
        response -> cancelReviewResponse(response, reviewId, ReviewLikeStatusEnum.LIKE),
        () -> {
          saveReviewResponse(review, user, reviewId, ReviewLikeStatusEnum.LIKE);
        });

    return result;
  }


  @Transactional
  public boolean dislikeReview(Long categoryId, Long productId,
      Long reviewId, User user) {
    ReviewLikeResponseDto responseDto = null;
    categoryRepository.existsById(categoryId);
    Product product = productRepository.findProductByIdOrElseThrow(productId);
    checkProductCategoryAndCategoryIdEquality(product, categoryId);
    Review review = reviewRepository.findReviewByIdOrElseThrow(reviewId);
    checkReviewProductAndProductIdEquality(review, productId);

    if (user.getId().equals(review.getUser().getId())) {
      throw new ApiException("본인의 리뷰에 비추천 누를 수 없습니다.", HttpStatus.BAD_REQUEST);
    }

    Optional<ReviewLike> reviewResponse = reviewLikeRepository.findByReviewAndUser(review, user);

    reviewResponse.ifPresentOrElse(
        response ->
        {
          cancelReviewResponse(response, reviewId, ReviewLikeStatusEnum.DISLIKE);
        },
        () -> saveReviewResponse(review, user, reviewId, ReviewLikeStatusEnum.DISLIKE));

    return result;
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

  void cancelReviewResponse(ReviewLike response, Long reviewId, ReviewLikeStatusEnum statusEnum) {
    if (response.getStatus().equals(statusEnum)) {
      reviewLikeRepository.delete(response);
      if (statusEnum == ReviewLikeStatusEnum.LIKE) {
        reviewRepository.decrementLikesCount(reviewId);//리뷰 추천 수 감소
        notificationService.deleteNotificationByEntity("REVIEW", reviewId);
      }
      result = false;
    } else {
      if (statusEnum == ReviewLikeStatusEnum.LIKE) {
        throw new ApiException("이미 비추천을 눌렀습니다", HttpStatus.BAD_REQUEST);
      } else {
        throw new ApiException("이미 추천을 눌렀습니다", HttpStatus.BAD_REQUEST);
      }
    }
  }

  void saveReviewResponse(Review review, User user, Long reviewId,
      ReviewLikeStatusEnum statusEnum) {
    ReviewLike newReviewLike = new ReviewLike(review, user);
    newReviewLike.setStatus(statusEnum);
    reviewLikeRepository.save(newReviewLike);

    if (statusEnum == ReviewLikeStatusEnum.LIKE) {
      reviewRepository.incrementLikesCount(reviewId);//리뷰 추천 수 증가
      notificationService.send(review.getUser(), "REVIEW", reviewId, user);
    }

    result = true;
  }
}
