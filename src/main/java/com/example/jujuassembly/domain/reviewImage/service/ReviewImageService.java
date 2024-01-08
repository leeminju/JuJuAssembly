package com.example.jujuassembly.domain.reviewImage.service;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.domain.reviewImage.repository.ReviewImageRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.s3.S3Manager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j(topic = "리뷰 사진 업로드 서비스")
@Service
@RequiredArgsConstructor
public class ReviewImageService {

  private final ReviewImageRepository reviewImageRepository;
  private final S3Manager s3Manager;

  @Transactional(propagation = Propagation.MANDATORY)
  public void uploadImages(Review review, MultipartFile[] images) throws Exception {
    int length = images.length;
    int count = 0;
    for (MultipartFile image : images) {
      if (image.getContentType() == null) {
        continue;
      }

      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일이 아닙니다.", HttpStatus.BAD_REQUEST);
      }

      String imageUrl = s3Manager.uploadMultipartFileWithPublicRead(
          "reviews/" + review.getId().toString() + "/",
          image
      );

      ReviewImage postImage = new ReviewImage(review, imageUrl);
      reviewImageRepository.save(postImage);
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteImage(Review review, String fileUrl) {
    s3Manager.deleteReviewImageFile(fileUrl);
    for (ReviewImage image : review.getReviewImages()) {
      if (image.getImageUrl().equals(fileUrl)) {
        review.getReviewImages().remove(image);
        reviewImageRepository.delete(image);
        break;
      }
    }
  }

  // 리뷰의 모든 파일 삭제
  public void deleteAll(Long reviewId) {
    s3Manager.deleteAllReviewImageFiles(reviewId.toString());
  }
}
