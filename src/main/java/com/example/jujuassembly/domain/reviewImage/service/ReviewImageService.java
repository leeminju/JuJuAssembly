package com.example.jujuassembly.domain.reviewImage.service;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.domain.reviewImage.repository.ReviewImageRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.s3.S3Manager;
import java.util.List;
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
  private final ReviewRepository reviewRepository;

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


  //사진 하나만 삭제
  @Transactional
  public void deleteReviewImage(Long reviewId, Long imageIndex) {
    Review review = reviewRepository.findReviewByIdOrElseThrow(reviewId);
    List<ReviewImage> reviewImages = reviewImageRepository.findReviewImageByReview(review);
    if (imageIndex == null || imageIndex >= reviewImages.size()) {
      throw new ApiException("유효하지 않은 이미지 인덱스입니다.", HttpStatus.BAD_REQUEST);
    }
    ReviewImage image = reviewImages.get(imageIndex.intValue());
    String imageUrl = image.getImageUrl();

    reviewImageRepository.delete(image);
    s3Manager.deleteImageFile(imageUrl, S3Manager.REVIEW_DIRECTORY_NAME);
  }


  // 디렉토리의 모든 파일 삭제
  @Transactional
  public void deleteAllReviewImages(Review review, String dirName) {
    review.getReviewImages().removeAll(review.getReviewImages());
    reviewImageRepository.deleteAllByReview(review);
    s3Manager.deleteAllImageFiles(review.getId().toString(), dirName);
  }


  //기존 리뷰 이미지 가져오기
  @Transactional
  public List<ReviewImage> findImageFromReviewImage(Review review) {
    List<ReviewImage> existingReviewImagesList = reviewImageRepository.findReviewImageByReview(
        review);
    return existingReviewImagesList;
  }
}

