package com.example.jujuassembly.domain.reviewLike.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.entity.Category;
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
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.exception.ApiException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReviewLikeServiceTest {

  @Mock
  CategoryRepository categoryRepository;
  @Mock
  ProductRepository productRepository;
  @Mock
  ReviewRepository reviewRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  ReviewLikeRepository reviewLikeRepository;
  @Mock
  NotificationRepository notificationRepository;
  @Mock
  NotificationService notificationService;
  @InjectMocks
  ReviewLikeService reviewLikeService;

  private Long categoryId = 1L;
  private Long productId = 2L;
  private Long reviewId = 3L;
  private Long reviewLikeId = 4L;
  private Long userId = 5L;
  private Long reviewUserId = 6L;
  private String loginId = "tester";
  private String reviewUserLoginId = "tester2";
  private User user;
  private User reviewUser;
  private Product product;
  private Category category;
  private Review review;


  @BeforeEach
  void setUp() {
    // Mock 객체 생성 및 초기화
    user = User.builder().id(userId).loginId(loginId).build();
    reviewUser = User.builder().id(reviewUserId).loginId(reviewUserLoginId).build();

    category = Category.builder().id(categoryId).build();
    product = Product.builder().id(productId).category(category).build();
    review = Review.builder().id(reviewId).product(product).user(reviewUser).build();

    when(productRepository.findProductByIdOrElseThrow(productId)).thenReturn(product);
    when(categoryRepository.existsById(categoryId)).thenReturn(true);
    when(reviewRepository.findReviewByIdOrElseThrow(reviewId)).thenReturn(review);
  }

  @Test
  @DisplayName("리뷰 추천 테스트 - 아무 기록 없을 때 ")
  void likeReviewTest() {
    // given
    ReviewLike mockReviewLike = ReviewLike.builder().id(reviewLikeId).user(user).review(review)
        .status(ReviewLikeStatusEnum.LIKE)
        .build();

    when(reviewLikeRepository.findByReviewAndUser(review, user)).thenReturn(
        Optional.empty());

    when(reviewLikeRepository.save(any(ReviewLike.class))).thenReturn(mockReviewLike);
    // when
    boolean result = reviewLikeService.likeReview(categoryId,
        productId,
        reviewId, user);

    // then
    assertTrue(result);
  }

  @Test
  @DisplayName("리뷰 추천 테스트 - 비추천 기록 있을 때 ")
  void likeReviewTest2() {
    // given
    ReviewLike mockReviewLike = ReviewLike.builder().id(reviewLikeId).user(user).review(review)
        .status(ReviewLikeStatusEnum.DISLIKE)
        .build();

    when(reviewLikeRepository.findByReviewAndUser(review, user)).thenReturn(
        Optional.of(mockReviewLike));

    // when
    ApiException exception = assertThrows(ApiException.class, () -> {
      reviewLikeService.likeReview(categoryId,
          productId, reviewId, user);
    });
    // then
    assertEquals("이미 비추천을 눌렀습니다", exception.getMsg());
  }

  @Test
  @DisplayName("리뷰 추천 해제 테스트")
  void unlikeReviewTest() {
    // given
    ReviewLike reviewLike = ReviewLike.builder().id(reviewLikeId).user(user)
        .status(ReviewLikeStatusEnum.LIKE)
        .build();

    when(reviewLikeRepository.findByReviewAndUser(review, user)).thenReturn(
        Optional.of(reviewLike));

    // when
    boolean result = reviewLikeService.likeReview(categoryId, productId,
        reviewId, user);

    // then
    assertFalse(result);
    verify(reviewLikeRepository, times(1)).delete(reviewLike);
  }

  @Test
  @DisplayName("리뷰 비추천 테스트 - 아무 기록 없을 떄")
  void dislikeReviewTest() {
    // given
    ReviewLike mockReviewLike = ReviewLike.builder().id(reviewLikeId).user(user).review(review)
        .status(ReviewLikeStatusEnum.DISLIKE)
        .build();

    when(reviewLikeRepository.findByReviewAndUser(review, user)).thenReturn(
        Optional.empty());

    when(reviewLikeRepository.save(any(ReviewLike.class))).thenReturn(mockReviewLike);
    // when
    boolean result = reviewLikeService.dislikeReview(categoryId,
        productId,
        reviewId, user);

    // then
    assertTrue(result);
  }

  @Test
  @DisplayName("리뷰 비추천 테스트 - 추천 기록 있을 떄")
  void dislikeReviewTest2() {
    // given
    ReviewLike mockReviewLike = ReviewLike.builder().id(reviewLikeId).user(user).review(review)
        .status(ReviewLikeStatusEnum.LIKE)
        .build();

    when(reviewLikeRepository.findByReviewAndUser(review, user)).thenReturn(
        Optional.of(mockReviewLike));

    // when
    ApiException exception = assertThrows(ApiException.class, () -> {
      reviewLikeService.dislikeReview(categoryId,
          productId, reviewId, user);
    });
    // then
    assertEquals("이미 추천을 눌렀습니다", exception.getMsg());
  }


  @Test
  @DisplayName("리뷰 비추천 해제 테스트")
  void undislikeReviewTest() {
    // given
    ReviewLike reviewLike = ReviewLike.builder().id(reviewLikeId).user(user)
        .status(ReviewLikeStatusEnum.DISLIKE)
        .build();

    when(reviewLikeRepository.findByReviewAndUser(review, user)).thenReturn(
        Optional.of(reviewLike));

    // when
    boolean result = reviewLikeService.dislikeReview(categoryId, productId,
        reviewId, user);

    // then
    assertFalse(result);
    verify(reviewLikeRepository, times(1)).delete(reviewLike);
  }

}