package com.example.jujuassembly.domain.reviewImage.repository;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.global.exception.ApiException;
import java.util.List;
import javax.swing.text.html.parser.Element;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

  void deleteAllByReview(Review review);

  void deleteByReview(Review review);

  default ReviewImage getById(Long id) { //findReviewImageByIdOrElseThrow로 수정
    return findById(id).orElseThrow(
        () -> new ApiException("해당 사진을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    );
  }
  List<ReviewImage> findReviewImageByReview(Review review);


  /*default List<ReviewImage> findReviewImageByReviewId(Long reviewId){
    return  findAllByReview(reviewId).orElseThrow()

  }*/
}
