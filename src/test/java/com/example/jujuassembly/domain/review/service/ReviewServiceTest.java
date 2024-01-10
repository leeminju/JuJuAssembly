package com.example.jujuassembly.domain.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.review.dto.ReviewRequestDto;
import com.example.jujuassembly.domain.review.dto.ReviewResponseDto;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.domain.reviewImage.service.ReviewImageService;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLikeStatusEnum;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
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

  private Long categoryId = 1L;
  private Long productId = 1L;

  private User user;
  private User user2;
  private Category category1;
  private Category category2;

  private Product product;

  @BeforeEach
  void setUp() {
    MultipartFile category_image1 = mock(MultipartFile.class);
    MultipartFile category_image2 = mock(MultipartFile.class);
    MultipartFile review_image = mock(MultipartFile.class);
    category1 = Category.builder().id(1L).name("소주").image(category_image1.getName()).build();

    category2 = Category.builder().id(2L).name("맥주").image(category_image1.getName()).build();

    product = Product.builder().id(productId).description("소주의 원조").company("진로하이트").area("서울")
        .name("참이슬").image(review_image.getName()).category(category1).alcoholDegree(16.5).build();

    user = User.builder().id(1L).loginId("tester").nickname("nickname").email("email@naver.com")
        .password("password").firstPreferredCategory(category1).secondPreferredCategory(category2)
        .build();

    user2 = User.builder().id(2L).loginId("tester2").nickname("nickname2").email("email2@naver.com")
        .password("password").firstPreferredCategory(category1).secondPreferredCategory(category2)
        .build();
  }

  @Test
  @DisplayName("리뷰 생성 테스트")
  void createProductsReview() throws Exception {
    //given
    ReviewRequestDto requestDto = ReviewRequestDto.builder().description("리뷰 내용").star(2.5)
        .munchies("안주").build();
    MultipartFile image1 = mock(MultipartFile.class);
    MultipartFile image2 = mock(MultipartFile.class);
    MultipartFile[] images = {image1, image2};

    Set<ReviewImage> reviewImages = new LinkedHashSet<>();
    reviewImages.add(ReviewImage.builder().id(1L).imageUrl(image1.getName()).build());
    reviewImages.add(ReviewImage.builder().id(2L).imageUrl(image2.getName()).build());

    Set<ReviewLike> reviewLikes = new LinkedHashSet<>();
    reviewLikes.add(
        ReviewLike.builder().id(1L).status(ReviewLikeStatusEnum.LIKE).user(user).build());
    reviewLikes.add(
        ReviewLike.builder().id(2L).status(ReviewLikeStatusEnum.DISLIKE).user(user2).build());

    ReviewService reviewService = new ReviewService(categoryRepository, productRepository,
        reviewRepository, userRepository, reviewImageService);

    Review review = Review.builder().id(1L).description(requestDto.getDescription())
        .star(requestDto.getStar())
        .munchies(requestDto.getMunchies()).product(product).user(user).reviewImages(reviewImages)
        .reviewLikes(reviewLikes)
        .build();

    given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category1));
    given(productRepository.findById(categoryId)).willReturn(Optional.of(product));
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
  void getProductsReview() {
  }

  @Test
  void updateProductsReview() {
  }

  @Test
  void deleteProductsReview() {
  }

  @Test
  void getMyReviews() {
  }

  @Test
  void verifyReview() {
  }
}