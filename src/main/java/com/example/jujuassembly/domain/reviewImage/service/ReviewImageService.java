package com.example.jujuassembly.domain.reviewImage.service;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.domain.reviewImage.repository.ReviewImageRepository;
import com.example.jujuassembly.global.s3.S3Manager;
import lombok.RequiredArgsConstructor;
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
  public void uploadImages(Review review, MultipartFile[] images) throws Exception {
    for (MultipartFile image : images) {
      if (image.getContentType() == null) {
        throw new RuntimeException("이미지가 아닙니다.");
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
    s3Manager.deletePostFile(fileUrl);
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
    s3Manager.deleteAllPostFiles(reviewId.toString());
  }

}
