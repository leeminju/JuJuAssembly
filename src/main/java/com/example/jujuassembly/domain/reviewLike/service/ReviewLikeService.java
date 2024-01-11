package com.example.jujuassembly.domain.reviewLike.service;

import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.reviewLike.dto.ReviewLikeResponseDto;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLikeStatusEnum;
import com.example.jujuassembly.domain.reviewLike.repository.ReviewLikeRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
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
  private final UserRepository userRepository;
  private final ReviewLikeRepository reviewLikeRepository;

  @Transactional
  public Optional<ReviewLikeResponseDto> likeReview(Long categoryId, Long productId, Long reviewId,
      User user) {
    ReviewLikeResponseDto responseDto = null;
    validateCategory(categoryId);
    Product product = validateProduct(productId);
    validateProductCategory(product, categoryId);
    Review review = validateReview(reviewId);
    validateProductReview(review, productId);

    Optional<ReviewLike> reviewLike = reviewLikeRepository.findByReviewAndUser(review, user);

    if (reviewLike.isPresent()) {
      if (reviewLike.get().getStatus().equals(ReviewLikeStatusEnum.LIKE)) {
        reviewLikeRepository.delete(reviewLike.get());//이미 추천을 눌렀다면 추천 해제
        return Optional.empty();
      } else {
        reviewLike.get().like();//비추천 -> 추천으로 변경
        responseDto = new ReviewLikeResponseDto(reviewLike.get());
      }
    } else {
      ReviewLike newReviewLike = new ReviewLike(review, user);
      newReviewLike.like();
      ReviewLike savedReviewLike = reviewLikeRepository.save(newReviewLike);
      responseDto = new ReviewLikeResponseDto(savedReviewLike);
    }

    return Optional.of(responseDto);
  }

  @Transactional
  public Optional<ReviewLikeResponseDto> dislikeReview(Long categoryId, Long productId,
      Long reviewId, User user) {
    ReviewLikeResponseDto responseDto = null;
    validateCategory(categoryId);
    Product product = validateProduct(productId);
    validateProductCategory(product, categoryId);
    Review review = validateReview(reviewId);
    validateProductReview(review, productId);

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
      ReviewLike savedReviewLike= reviewLikeRepository.save(newReviewLike);
      responseDto = new ReviewLikeResponseDto(savedReviewLike);
    }

    return Optional.of(responseDto);
  }

  //카테고리 존재 검증
  private void validateCategory(Long categoryId) {
    categoryRepository.findById(categoryId).orElseThrow(
        () -> new ApiException("해당 카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    );
  }

  //상품 존재 검증
  private Product validateProduct(Long productId) {
    Product product = productRepository.findById(productId).orElseThrow(
        () -> new ApiException("해당 주류를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    );
    return product;
  }

  //리뷰 존재 검증
  private Review validateReview(Long reviewId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(
        () -> new ApiException("해당하는 리뷰가 존재하지 않습니다.", HttpStatus.NOT_FOUND)
    );
    return review;
  }

  //상품 카테고리 일치 검증
  private void validateProductCategory(Product product, Long categoryId) {
    if (!Objects.equals(product.getCategory().getId(), categoryId)) {
      throw new ApiException("해당 카테고리 상품이 아닙니다.", HttpStatus.BAD_REQUEST);
    }
  }

  //리뷰와 상품 일치 검증
  private void validateProductReview(Review review, Long productId) {
    if (!Objects.equals(review.getProduct().getId(), productId)) {
      throw new ApiException("해당 상품의 리뷰가 아닙니다.", HttpStatus.BAD_REQUEST);
    }
  }


}
