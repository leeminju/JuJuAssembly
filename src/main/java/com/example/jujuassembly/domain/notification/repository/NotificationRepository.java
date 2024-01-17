package com.example.jujuassembly.domain.notification.repository;

import com.example.jujuassembly.domain.notification.entity.Notification;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findAllByUserId(Long userId);

  // 리뷰와 사용자에 해당하는 알림을 삭제하는 메서드
  void deleteByReviewAndUser(Review review, User user);

  // 해당 리뷰 관련 알림 모두 삭제
  void deleteByReview(Review review);
}
