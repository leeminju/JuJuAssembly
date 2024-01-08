package com.example.jujuassembly.domain.review.service;

import com.example.jujuassembly.domain.category.entity.repository.CategoryRepository;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.review.dto.ReviewRequestDto;
import com.example.jujuassembly.domain.review.dto.ReviewResponseDto;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.reviewImage.service.ReviewImageService;
import com.example.jujuassembly.domain.user.entity.User;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final ReviewRepository reviewRepository;
  private final ReviewImageService reviewImageService;

  @Transactional
  public ReviewResponseDto createProductsReview(Long categoryId, Long productId,
      MultipartFile[] images, ReviewRequestDto requestDto, User user) {
    if (images.length > 4) {
      throw new IllegalArgumentException("사진은 4장까지만 업로드 가능합니다.");
    }

    categoryRepository.findById(categoryId).orElseThrow(
        () -> new NullPointerException("해당 카테고리를 찾을 수 없습니다.")
    );

    Product product = productRepository.findById(productId).orElseThrow(
        () -> new NullPointerException("해당 주류를 찾을 수 없습니다.")
    );
    if (!Objects.equals(product.getCategory().getId(), categoryId)) {
      throw new NullPointerException("해당 카테고리 상품이 아닙니다.");
    }

    Review review = new Review(requestDto, product, user);

    Review savedReview = reviewRepository.save(review);
    // 이미지 업로드
    try {
      reviewImageService.uploadImages(savedReview, images);
    } catch (Exception e) {
      // 예외 발생시 S3와 DB 사이의 데이터 무결성을 보장하지 않고 주기적으로 스케쥴러로 처리한다
      // 즉각적인 무결성을 보장하기 위한 비용이 스케쥴링으로 처리하는 비용보다 비싸다
      throw new RuntimeException("이미지를 읽을 수 없습니다.");
    }

    return new ReviewResponseDto(savedReview);
  }
}
