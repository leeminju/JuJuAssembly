package com.example.jujuassembly.domain.like.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.CategoryTest;
import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.like.dto.LikeResponseDto;
import com.example.jujuassembly.domain.like.entity.Like;
import com.example.jujuassembly.domain.like.repository.LikeRepository;
import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.review.service.ReviewService;
import com.example.jujuassembly.domain.reviewImage.service.ReviewImageService;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest implements CategoryTest {

  @Mock
  ProductRepository productRepository;
  @Mock
  LikeRepository likeRepository;
  @Mock
  UserRepository userRepository;


  @Mock
  CategoryRepository categoryRepository;

  @Mock
  ReviewRepository reviewRepository;
  @Mock
  ReviewImageService reviewImageService;
  @Mock
  NotificationService notificationService;

  @InjectMocks
  ReviewService reviewService;

  private Category category;
  private ProductRequestDto requestDto;
  private Set<Review> reviews;
  private Set<Like> likes;


  @InjectMocks
  LikeService likeService;

  @Test
  @DisplayName("좋아요 테스트")
  void addLikeTest() {

    // given
    Long categoryId = 1L;
    Long productId = 100L;
    Long userId = 1L;

    Product mockProduct = mock(Product.class);
    Category mockCategory = mock(Category.class);
    User mockUser = mock(User.class);

    when(mockProduct.getCategory()).thenReturn(mockCategory);
    when(mockCategory.getId()).thenReturn(categoryId);
    when(productRepository.getById(productId)).thenReturn(mockProduct);
    when(mockUser.getId()).thenReturn(userId);
    when(likeRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

    // when
    likeService.addLike(categoryId, productId, mockUser);

    // then
    verify(likeRepository, times(1)).save(any(Like.class));


  }

  @Test
  @DisplayName("좋아요 상품 조회 테스트")
  void viewLikeProductsTest() {
    // given
    Long userId = 123L;
    User loginUser = User.builder().id(userId).build();
    Pageable pageable = PageRequest.of(0, 10);

    User user = User.builder().id(userId).build();

    List<Like> likeList = new ArrayList<>();
    Like like1 = Like.builder().id(1L).user(user).product(createProductWithLikes(1L)).build();
    likeList.add(like1);

    Like like2 = Like.builder().id(2L).user(user).product(createProductWithLikes(2L)).build();
    likeList.add(like2);

    Page<Like> mockReport = new PageImpl<>(likeList, pageable, likeList.size());

    when(likeRepository.findAllByUser(user, pageable)).thenReturn(mockReport);
    when(userRepository.getById(userId)).thenReturn(user);
    //when(likeRepository.findAllByUserId(userId)).thenReturn(likeList);

    // when
    Page<LikeResponseDto> likeResult = likeService.viewLikeProducts(userId, loginUser, pageable);

    // then
    assertEquals(likeList.size(), likeResult.getContent().size(), "좋아요 개수 검증");

    // 각 리뷰에 대한 추가적인 검증 로직을 작성
    //  첫 번째 리뷰에 대한 검증
    LikeResponseDto firstLike = likeResult.getContent().get(0);
    assertEquals(like1.getId(), firstLike.getId(), "좋아요1 Id 검증");
    assertEquals(like1.getProduct().getName(), firstLike.getProductName(), "좋아요 상품명 검증");

    // 두 번째 리뷰에 대한 검증도 동일한 방식으로 수행
    LikeResponseDto secondLike = likeResult.getContent().get(1);
    assertEquals(like2.getId(), secondLike.getId(), "좋아요2 Id 검증");
    assertEquals(like2.getProduct().getName(), secondLike.getProductName(), "좋아요 상품명 검증");
  }

  private Product createProductWithLikes(Long productId) {
    Set<Like> likes = new HashSet<>();
    likes.add(Like.builder().id(productId).user(User.builder().id(1L).build()).build());

    return Product.builder()
        .id(productId)
        .image("ualsl")
        .name("진로")
        .description("a")
        .area("a")
        .company("a")
        .alcoholDegree(3.3)
        .category(Category.builder().id(1L).build())
        .reviews(new HashSet<>())
        .likes(likes)
        .build();
  }

  @Test
  @DisplayName("좋아요 취소 테스트")
  void cancelLikeTest() {
    // 목 데이터 생성
    Long productId = 1L;
    Product product = Product.builder().id(productId).name("카스").build();
    User user = User.builder().id(123L).build();
    Like like = Like.builder().user(user).product(product).build();

    // Mocking 설정
    when(productRepository.getById(productId)).thenReturn(product);
    when(likeRepository.findByProductAndUser(product, user)).thenReturn(Optional.of(like));

    // 테스트 대상 메서드 호출
    likeService.cancelLike(productId, user);

    // 검증
    verify(likeRepository, times(1)).delete(like);
  }

}