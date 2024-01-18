package com.example.jujuassembly.domain.notification.repository;

import com.example.jujuassembly.domain.notification.entity.Notification;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findAllByUserId(Long userId);
  
  // 해당 type의 관련된 알림 삭제
  List<Notification> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
