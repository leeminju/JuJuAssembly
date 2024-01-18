package com.example.jujuassembly.domain.reviewImage.service;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.domain.reviewImage.repository.ReviewImageRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.s3.S3Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewImageService {

  private final ReviewImageRepository reviewImageRepository;
  private final S3Manager s3Manager;

  @Transactional(propagation = Propagation.MANDATORY)
  public void uploadReviewImages(Review review, MultipartFile[] images) throws Exception {
    for (MultipartFile image : images) {
      if (image.getContentType() == null) {
        continue;
      }

      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일이 아닙니다.", HttpStatus.BAD_REQUEST);
      }

      String imageUrl = s3Manager.uploadMultipartFileWithPublicRead(
          S3Manager.REVIEW_PREFIX + review.getId().toString() + "/",
          image
      );

      ReviewImage reviewImage = new ReviewImage(review, imageUrl);
      reviewImageRepository.save(reviewImage);
    }
  }


  // 디렉토리의 모든 파일 삭제
  public void deleteAllReviewImages(Review review, String dirName) {
    review.getReviewImages().removeAll(review.getReviewImages());
    reviewImageRepository.deleteAllByReview(review);
    s3Manager.deleteAllImageFiles(review.getId().toString(), dirName);
  }
}

