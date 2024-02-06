package com.example.jujuassembly.domain.review.repository;

import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.exception.ApiException;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  default Review findReviewByIdOrElseThrow(Long id) {
    return findById(id).orElseThrow(
        () -> new ApiException("해당하는 리뷰가 없습니다.", HttpStatus.NOT_FOUND)
    );
  }

  Page<Review> findAllByUser(User user, Pageable pageable);

  Page<Review> findAllByProduct(Product product, Pageable pageable);

  Page<Review> findAllByUserAndIsVerified(User user, boolean b, Pageable pageable);

  @Modifying
  @Query("UPDATE Review SET isVerified = :isVerified WHERE id = :reviewId")
  void verifyReview(@Param("reviewId") Long reviewId, @Param("isVerified") Boolean isVerified);

  //리뷰 추천 수 증가
  @Modifying
  @Query("UPDATE Review r SET r.likeCount = r.likeCount+1 WHERE r.id = :reviewId")
  void increaseLikesCount(@Param("reviewId") Long reviewId);

  //리뷰 추천 수 감소
  @Modifying
  @Query("UPDATE Review r SET r.likeCount = r.likeCount-1 WHERE r.id = :reviewId")
  void decreaseLikesCount(@Param("reviewId") Long reviewId);

  List<Review> findAllByProduct_Id(Long productId);

}
