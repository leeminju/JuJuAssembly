package com.example.jujuassembly.global.aop;

import com.example.jujuassembly.domain.report.entity.Report;
import com.example.jujuassembly.domain.report.repository.ReportRepository;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

  private final ReviewRepository reviewRepository;
  private final ReportRepository reportRepository;
  private final UserRepository userRepository;

  @Pointcut("execution(public * com.example.jujuassembly.domain.review.controller.ReviewController.updateProductsReview(..)) && args(reviewId, .., userDetails)")
  public void updateProductsReviewPointcut(Long reviewId, UserDetailsImpl userDetails) {
  }

  @Pointcut("execution(public * com.example.jujuassembly.domain.review.controller.ReviewController.deleteProductsReview(..)) && args(reviewId, .., userDetails)")
  public void deleteProductsReviewPointcut(Long reviewId, UserDetailsImpl userDetails) {
  }

  @Pointcut("execution(public * com.example.jujuassembly.domain.report.controller.ReportController.patchReport(..)) && args(reportId, .., userDetails)")
  public void patchReportPointcut(Long reportId, UserDetailsImpl userDetails) {
  }

  @Pointcut("execution(public * com.example.jujuassembly.domain.report.controller.ReportController.deleteReport(..)) && args(reportId, .., userDetails)")
  public void deleteReportPointcut(Long reportId, UserDetailsImpl userDetails) {
  }

  @Pointcut("execution(public * com.example.jujuassembly.domain.report.controller.ReportController.getReports(..)) && args(userId, userDetails)")
  public void getReportsPointcut(Long userId, UserDetailsImpl userDetails) {
  }


  @Before("updateProductsReviewPointcut(reviewId, userDetails) || deleteProductsReviewPointcut(reviewId, userDetails)")
  public void checkReviewAccessibility(Long reviewId,
      UserDetailsImpl userDetails) {

    User user = userDetails.getUser();
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ApiException("review가 존재하지 않습니다.", HttpStatus.NOT_FOUND));

    if (!review.getUser().getId().equals(user.getId())) {
      if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
        throw new ApiException("관리자 또는 리뷰 작성자만 접근 할 수 있습니다.", HttpStatus.UNAUTHORIZED);
      }
    }
  }

  @Before("patchReportPointcut(reportId, userDetails) || deleteReportPointcut(reportId, userDetails)")
  public void checkReportAccessibility(Long reportId,
      UserDetailsImpl userDetails) {

    User user = userDetails.getUser();
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new ApiException("report가 존재하지 않습니다.", HttpStatus.NOT_FOUND));

    if (!report.getUser().getId().equals(user.getId())) {
      if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
        throw new ApiException("관리자 또는 제보 작성자만 접근 할 수 있습니다.", HttpStatus.UNAUTHORIZED);
      }
    }
  }

  @Before("getReportsPointcut(userId, userDetails)")
  public void checkToGetReportAccessibility(Long userId,
      UserDetailsImpl userDetails) {

    User accessingUser = userDetails.getUser();
    User reportUser = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException("해당 userId가 존재하지 않습니다.", HttpStatus.NOT_FOUND));

    if (!accessingUser.getId().equals(reportUser.getId())) {
      if (!accessingUser.getRole().equals(UserRoleEnum.ADMIN)) {
        throw new ApiException("관리자 또는 본인만 접근 할 수 있습니다.", HttpStatus.UNAUTHORIZED);
      }
    }
  }


}
