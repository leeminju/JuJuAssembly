package com.example.jujuassembly.domain.reviewLike.repository;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike,Long> {
  Optional<ReviewLike> findByReviewAndUser(Review review, User user);
}
