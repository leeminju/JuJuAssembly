package com.example.jujuassembly.domain.notification.repository;

import com.example.jujuassembly.domain.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// NotificationRepository.java
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findAllByUserId(Long userId);
}
