package com.example.jujuassembly.global.aop;

import com.example.jujuassembly.domain.review.service.ReviewService;
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

  private final ReviewService reviewService;

  @Pointcut("execution(public * com.example.jujuassembly.domain.review.controller.ReviewController.updateProductsReview(..)) && args(.., authenticationPrincipal)")
  public void updateProductsReviewPointcut(AuthenticationPrincipal authenticationPrincipal) {
  }

  @Pointcut("execution(public * com.example.jujuassembly.domain.review.controller.ReviewController.deleteProductsReview(..)) && args(.., authenticationPrincipal)")
  public void deleteProductsReviewPointcut(AuthenticationPrincipal authenticationPrincipal) {
  }

  @Before("updateProductsReviewPointcut(authenticationPrincipal) || deleteProductsReviewPointcut(authenticationPrincipal)")
  public void beforeUpdateProductsReview(AuthenticationPrincipal authenticationPrincipal) {
    UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authenticationPrincipal;
    if (!userDetailsImpl.getUser().getRole().getAuthority().equals(UserRoleEnum.ADMIN)) {
      throw new ApiException("관리자만 접근 할 수 있습니다.", HttpStatus.UNAUTHORIZED);
    }
  }


}
