package com.example.jujuassembly.domain.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.entity.Category;
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
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

  @Mock
  CategoryRepository categoryRepository;
  @Mock
  ProductRepository productRepository;
  @Mock
  ReviewRepository reviewRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  ReviewImageService reviewImageService;
  @Mock
  NotificationService notificationService;

  @InjectMocks
  ReviewService reviewService;

  private Long categoryId = 1L;
  private Long reviewId = 1L;
  private Long productId = 1L;
  private Long userId = 1L;
  private User user;
  private User user2;
  private Category category1;
  private Category category2;
  private Product product;
  private Product product2;
  private Pageable pageable;
  private Pageable pageable2;
  private Set<ReviewImage> reviewImages;
  private Set<ReviewLike> reviewLikes;
  private MultipartFile[] images;

  @BeforeEach
  void setUp() {
    MultipartFile category_image1 = mock(MultipartFile.class);
    MultipartFile category_image2 = mock(MultipartFile.class);
    MultipartFile review_image = mock(MultipartFile.class);
    category1 = Category.builder().id(categoryId).name("소주").image(category_image1.getName())
        .build();

    category2 = Category.builder().id(2L).name("맥주").image(category_image1.getName()).build();

    product = Product.builder().id(productId).description("소주의 원조").company("진로하이트").area("서울")
        .name("참이슬").image(review_image.getName()).category(category1).alcoholDegree(16.5).build();

    product2 = Product.builder().id(2L).description("새로").company("롯데칠성음료").area("서울")
        .name("참이슬").image(review_image.getName()).category(category1).alcoholDegree(16.0).build();

    user = User.builder().id(userId).loginId("tester").nickname("nickname").email("email@naver.com")
        .password("password").firstPreferredCategory(category1).secondPreferredCategory(category2)
        .build();

    user2 = User.builder().id(2L).loginId("tester2").nickname("nickname2").email("email2@naver.com")
        .password("password").firstPreferredCategory(category1).secondPreferredCategory(category2)
        .build();

    MultipartFile image1 = mock(MultipartFile.class);
    MultipartFile image2 = mock(MultipartFile.class);
    images = new MultipartFile[]{image1, image2};

    reviewImages = new LinkedHashSet<>();
    reviewLikes = new LinkedHashSet<>();
    pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
    pageable2 = PageRequest.of(0, 10, Sort.by("createdAt").descending().and(Sort.by("id")));
  }

  @Test
  @DisplayName("리뷰 생성 테스트")
  void createProductsReviewTest() throws Exception {
    //given
    ReviewRequestDto requestDto = ReviewRequestDto.builder().description("리뷰 내용").star(2.5)
        .munchies("안주").build();

    Review review = Review.builder().id(1L).description(requestDto.getDescription())
        .star(requestDto.getStar()).munchies(requestDto.getMunchies()).product(product).user(user)
        .reviewImages(reviewImages).reviewLikes(reviewLikes).build();

    given(categoryRepository.findCategoryByIdOrElseThrow(categoryId)).willReturn(category1);
    given(productRepository.findProductByIdOrElseThrow(productId)).willReturn(product);
    given(reviewRepository.save(any(Review.class))).willReturn(review);

    //when
    ReviewResponseDto responseDto = reviewService.createProductsReview(categoryId, productId,
        images, requestDto, user);

    //then
    assertEquals(requestDto.getDescription(), responseDto.getDescription());
    assertEquals(requestDto.getMunchies(), responseDto.getMunchies());
    assertEquals(requestDto.getStar(), responseDto.getStar());
  }

  @Test
  @DisplayName("리뷰 수정 테스트")
  void updateProductsReviewTest() throws Exception {
    ReviewRequestDto requestDto = ReviewRequestDto.builder().description("리뷰 내용 수정").star(3.0)
        .munchies("안주").build();

    Review review = Review.builder().id(reviewId).description(requestDto.getDescription())
        .star(requestDto.getStar()).munchies(requestDto.getMunchies()).product(product).user(user)
        .reviewImages(reviewImages).reviewLikes(reviewLikes).build();

    given(categoryRepository.findCategoryByIdOrElseThrow(categoryId)).willReturn(category1);
    given(productRepository.findProductByIdOrElseThrow(categoryId)).willReturn(product);
    given(reviewRepository.findReviewByIdOrElseThrow(reviewId)).willReturn(review);
    //when
    ReviewResponseDto responseDto = reviewService.updateProductsReview(categoryId, productId,
        reviewId, images, requestDto);

    //then
    assertEquals(requestDto.getDescription(), responseDto.getDescription());
    assertEquals(requestDto.getMunchies(), responseDto.getMunchies());
    assertEquals(requestDto.getStar(), responseDto.getStar());
  }

  @Test
  @DisplayName("리뷰 삭제 테스트")
  void deleteProductsReviewTest() {

    Review review = Review.builder().id(reviewId).description("설명")
        .star(2.0).munchies("안주").product(product).user(user)
        .reviewImages(reviewImages).reviewLikes(reviewLikes).build();

    given(categoryRepository.findCategoryByIdOrElseThrow(categoryId)).willReturn(category1);
    given(productRepository.findProductByIdOrElseThrow(productId)).willReturn(product);
    given(reviewRepository.findReviewByIdOrElseThrow(reviewId)).willReturn(review);
    //when
    reviewService.deleteProductsReview(categoryId, productId, reviewId);

    //then
    verify(reviewRepository).delete(any(Review.class));
    verify(notificationService).deleteNotificationByEntity("REVIEW", reviewId);
  }

  @Test
  @DisplayName("상품별 리뷰 조회")
  void getProductsReviewTest() {
    //given
    Review review = Review.builder().id(reviewId).description("맛있다")
        .star(4.0).munchies("조개탕").product(product).user(user)
        .reviewImages(reviewImages).reviewLikes(reviewLikes).build();
    Review review2 = Review.builder().id(2L).description("맛있어요!")
        .star(4.0).munchies("순대").product(product).user(user)
        .reviewImages(reviewImages).reviewLikes(reviewLikes).build();

    given(categoryRepository.findCategoryByIdOrElseThrow(categoryId)).willReturn(category1);
    given(productRepository.findProductByIdOrElseThrow(productId)).willReturn(product);
    // 가상의 Review 엔티티 리스트 생성
    List<Review> reviewList = Arrays.asList(review, review2);

    Page<Review> mockReviews = new PageImpl<>(reviewList, pageable2, reviewList.size());
    when(reviewRepository.findAllByProduct(product, pageable2)).thenReturn(mockReviews);

    //when
    Page<ReviewResponseDto> reviews = reviewService.getProductsReview(categoryId, productId,
        pageable);

    //then
    // 예상한 결과의 개수와 실제 결과의 개수가 일치하는지 검증
    assertEquals(reviewList.size(), reviews.getContent().size(), "리뷰 개수 검증");

    // 각 리뷰에 대한 추가적인 검증 로직을 작성
    //  첫 번째 리뷰에 대한 검증
    ReviewResponseDto firstReview = reviews.getContent().get(0);
    assertEquals(review.getId(), firstReview.getId(), "리뷰 ID 검증");
    assertEquals(review.getDescription(), firstReview.getDescription(), "리뷰 설명 검증");

    // 두 번째 리뷰에 대한 검증도 동일한 방식으로 수행
    ReviewResponseDto secondReview = reviews.getContent().get(1);
    assertEquals(review2.getId(), secondReview.getId(), "리뷰 ID 검증");
    assertEquals(review2.getDescription(), secondReview.getDescription(), "리뷰 설명 검증");
  }

  @Test
  @DisplayName("유저별 리뷰 조회")
  void getMyReviewsTest() {
    //given
    Review review = Review.builder().id(reviewId).description("맛있다")
        .star(4.0).munchies("조개탕").product(product).user(user)
        .reviewImages(reviewImages).reviewLikes(reviewLikes).build();
    Review review2 = Review.builder().id(2L).description("맛있어요!")
        .star(4.0).munchies("순대").product(product2).user(user)
        .reviewImages(reviewImages).reviewLikes(reviewLikes).build();

    given(userRepository.findUserByIdOrElseThrow(userId)).willReturn(user);

    List<Review> reviewList = Arrays.asList(review, review2);
    Page<Review> mockReviews = new PageImpl<>(reviewList, pageable2, reviewList.size());

    when(reviewRepository.findAllByUser(user, pageable2)).thenReturn(mockReviews);

    //when
    Page<ReviewResponseDto> reviews = reviewService.getMyReviews(userId, pageable);

    //then
    assertEquals(reviewList.size(), reviews.getContent().size(), "리뷰 개수 검증");
    // 각 리뷰에 대한 추가적인 검증 로직을 작성
    //  첫 번째 리뷰에 대한 검증
    ReviewResponseDto firstReview = reviews.getContent().get(0);
    assertEquals(review.getId(), firstReview.getId(), "리뷰 ID 검증");
    assertEquals(review.getDescription(), firstReview.getDescription(), "리뷰 설명 검증");

    // 두 번째 리뷰에 대한 검증도 동일한 방식으로 수행
    ReviewResponseDto secondReview = reviews.getContent().get(1);
    assertEquals(review2.getId(), secondReview.getId(), "리뷰 ID 검증");
    assertEquals(review2.getDescription(), secondReview.getDescription(), "리뷰 설명 검증");
  }

  @Test
  @DisplayName("인증된 리뷰만 조회 테스트")
  void getVerifiedReviewsTest() {
    // given
    Review verifiedReview = Review.builder()
        .id(1L)
        .description("인증된 리뷰")
        .star(4.5)
        .isVerified(true)
        .product(product)
        .reviewImages(reviewImages)
        .reviewLikes(reviewLikes)
        .user(user)
        .build();

    Review unverifiedReview = Review.builder()
        .id(2L)
        .description("인증되지 않은 리뷰")
        .star(3.5)
        .isVerified(false)
        .product(product)
        .reviewImages(reviewImages)
        .reviewLikes(reviewLikes)
        .user(user)
        .build();

    List<Review> verifiedReviews = Collections.singletonList(verifiedReview);
    Page<Review> verifiedReviewsPage = new PageImpl<>(verifiedReviews, pageable2,
        verifiedReviews.size());

    given(userRepository.findUserByIdOrElseThrow(userId)).willReturn(user);
    given(reviewRepository.findAllByUserAndIsVerified(user, true, pageable2)).willReturn(
        verifiedReviewsPage);

    // when
    Page<ReviewResponseDto> response = reviewService.getVerifiedReviews(userId, pageable, true);

    // then
    assertEquals(1, response.getTotalElements());
    assertEquals(verifiedReview.getDescription(), response.getContent().get(0).getDescription());
    assertTrue(response.getContent().get(0).getIsVerified());
  }

  @Test
  @DisplayName("리뷰 인증 처리")
  void verifyReview() {
    Review review = Review.builder().id(reviewId).description("맛있다")
        .star(4.0).munchies("조개탕").product(product).user(user).isVerified(false)
        .reviewImages(reviewImages).reviewLikes(reviewLikes).build();
    //given
    given(categoryRepository.findCategoryByIdOrElseThrow(categoryId)).willReturn(category1);
    given(productRepository.findProductByIdOrElseThrow(productId)).willReturn(product);
    given(reviewRepository.findReviewByIdOrElseThrow(reviewId)).willReturn(review);

    //when
    Boolean result = reviewService.verifyReview(categoryId, productId, reviewId);

    //then
    assertEquals(true, result);
  }

  @Test
  @DisplayName("리뷰 인증 취소")
  void unverifyReview() {
    Review review = Review.builder().id(reviewId).description("맛있다")
        .star(4.0).munchies("조개탕").product(product).user(user).isVerified(true)
        .reviewImages(reviewImages).reviewLikes(reviewLikes).build();
    //given
    given(categoryRepository.findCategoryByIdOrElseThrow(categoryId)).willReturn(category1);
    given(productRepository.findProductByIdOrElseThrow(productId)).willReturn(product);
    given(reviewRepository.findReviewByIdOrElseThrow(reviewId)).willReturn(review);

    //when
    Boolean result = reviewService.verifyReview(categoryId, productId, reviewId);

    //then
    assertEquals(false, result);
  }
}