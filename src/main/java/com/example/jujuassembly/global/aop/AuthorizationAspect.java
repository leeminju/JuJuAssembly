package com.example.jujuassembly.global.aop;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.review.service.ReviewService;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

  private final ReviewRepository reviewRepository;

  @Pointcut("execution(public * com.example.jujuassembly.domain.review.controller.ReviewController.updateProductsReview(..)) && args(reviewId, .., userDetails)")
  public void updateProductsReviewPointcut(Long reviewId,
      UserDetailsImpl userDetails) {
  }

  @Pointcut("execution(public * com.example.jujuassembly.domain.review.controller.ReviewController.deleteProductsReview(..)) && args(reviewId, .., userDetails)")
  public void deleteProductsReviewPointcut(Long reviewId,
      UserDetailsImpl userDetails) {
  }

  @Before("updateProductsReviewPointcut(reviewId, userDetails) || deleteProductsReviewPointcut(reviewId, userDetails)")
  public void checkAccessibility(Long reviewId,
      UserDetailsImpl userDetails) {

    User user = userDetails.getUser();
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ApiException("review가 존재하지 않습니다.", HttpStatus.NOT_FOUND));

    if (!review.getWriter().getId().equals(user.getId())) {
      if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
        throw new ApiException("관리자 또는 리뷰 작성자만 접근 할 수 있습니다.", HttpStatus.UNAUTHORIZED);
      }
    }
  }


}
