package com.example.jujuassembly.domain.like.service;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.like.dto.LikeResponseDto;
import com.example.jujuassembly.domain.like.entity.Like;
import com.example.jujuassembly.domain.like.repository.LikeRepository;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

  @Mock
  ProductRepository productRepository;
  @Mock
  LikeRepository likeRepository;
  @Mock
  UserRepository userRepository;

  private Category category;
  private ProductRequestDto requestDto;


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
    //given
    Long userId = 123L;
    Product product = Product.builder().build();
    User loginUser = User.builder().id(userId).build();

    User user = User.builder().id(userId).build();

    List<Like> likeList = new ArrayList<>();
    Like like1 = Like.builder().id(1L).user(user).product(product).build();
    likeList.add(like1);

    Like like2 = Like.builder().id(2L).user(user).product(product).build();
    likeList.add(like2);

    when(userRepository.getById(userId)).thenReturn(user);
    when(likeRepository.findAllByUserId(userId)).thenReturn(likeList);

    // when
    List<LikeResponseDto> result = likeService.viewLikeProducts(userId, loginUser);

    // then
    assertNotNull(result);
    assertEquals(likeList.size(), result.size());
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