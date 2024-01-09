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
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.exception.ApiException;
import java.util.List;
import java.util.Objects;
import jdk.jshell.spi.ExecutionControl.RunException;
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
  private final UserRepository userRepository;
  private final ReviewImageService reviewImageService;

  @Transactional
  public ReviewResponseDto createProductsReview(Long categoryId, Long productId,
      MultipartFile[] images, ReviewRequestDto requestDto, User user) throws Exception {
    if (images.length > 4) {
      throw new ApiException("사진은 4장 까지만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
    }

    validateCategory(categoryId);
    Product product = validateProduct(productId);
    validateProductCategory(product, categoryId);

    Review review = new Review(requestDto, product, user);
    Review savedReview = reviewRepository.save(review);
    // 이미지 업로드
    reviewImageService.uploadReviewImages(savedReview, images);

    return new ReviewResponseDto(savedReview);
  }

  public List<ReviewResponseDto> getProductsReview(Long categoryId, Long productId, User user) {
    validateCategory(categoryId);
    Product product = validateProduct(productId);
    validateProductCategory(product, categoryId);

    return product.getReviews().stream().map(ReviewResponseDto::new).toList();
  }

  @Transactional
  public ReviewResponseDto updateProductsReview(Long categoryId, Long productId, Long reviewId,
      MultipartFile[] images, ReviewRequestDto requestDto, User user) throws Exception {
    if (images.length > 4) {
      throw new ApiException("사진은 4장 까지만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
    }
    validateCategory(categoryId);
    Product product = validateProduct(productId);
    validateProductCategory(product, categoryId);
    Review review = validateReview(reviewId);
    validateProductReview(review, productId);

    if (!review.getWriter().getId().equals(user.getId())) {
      throw new ApiException("리뷰 수정은 작성자만 가능합니다.", HttpStatus.FORBIDDEN);
    }

    //기존의 파일 모두 삭제
    reviewImageService.deleteAllReviewImages(review, "reviews");

    //새로 업로드
    reviewImageService.uploadReviewImages(review, images);

    review.update(requestDto);

    return new ReviewResponseDto(review);
  }

  public void deleteProductsReview(Long categoryId, Long productId, Long reviewId, User user) {
    validateCategory(categoryId);
    Product product = validateProduct(productId);
    validateProductCategory(product, categoryId);
    Review review = validateReview(reviewId);
    validateProductReview(review, productId);

    if (!review.getWriter().getId().equals(user.getId())) {
      throw new ApiException("리뷰 삭제는 작성자만 가능합니다.", HttpStatus.FORBIDDEN);
    }
    //기존의 파일 모두 삭제
    reviewRepository.delete(review);
    reviewImageService.deleteAllReviewImages(review, "reviews");
  }

  public List<ReviewResponseDto> getMyReviews(Long userId) {
    User user = userRepository.findById(userId).orElseThrow(
        () -> new ApiException("해당하는 유저가 없습니다.", HttpStatus.NOT_FOUND)
    );

    List<Review> reviews = reviewRepository.findAllByWriterOrderByModifiedAtDesc(user);
    return reviews.stream().map(ReviewResponseDto::new).toList();
  }

  @Transactional
  public ReviewResponseDto verifyReview(Long categoryId, Long productId, Long reviewId) {
    validateCategory(categoryId);
    Product product = validateProduct(productId);
    validateProductCategory(product, categoryId);
    Review review = validateReview(reviewId);
    validateProductReview(review, productId);

    if (review.getIsVerified()) {
      throw new ApiException("이미 인증된 리뷰 입니다.", HttpStatus.BAD_REQUEST);
    }
    review.verify();

    return new ReviewResponseDto(review);
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
