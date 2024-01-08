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
import com.example.jujuassembly.global.exception.ApiException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final ReviewRepository reviewRepository;
  private final ReviewImageService reviewImageService;

  @Transactional
  public ReviewResponseDto createProductsReview(Long categoryId, Long productId,
      MultipartFile[] images, ReviewRequestDto requestDto, User user) throws Exception {
    if (images.length > 4) {
      throw  new ApiException("사진은 4장 까지만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
    }

    categoryRepository.findById(categoryId).orElseThrow(
        () -> new ApiException("해당 카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    );

    Product product = productRepository.findById(productId).orElseThrow(
        () -> new ApiException("해당 주류를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    );
    if (!Objects.equals(product.getCategory().getId(), categoryId)) {
      throw new ApiException("해당 카테고리 상품이 아닙니다.", HttpStatus.BAD_REQUEST);
    }

    Review review = new Review(requestDto, product, user);

    Review savedReview = reviewRepository.save(review);
    // 이미지 업로드

    reviewImageService.uploadImages(savedReview, images);

    return new ReviewResponseDto(savedReview);
  }
}
