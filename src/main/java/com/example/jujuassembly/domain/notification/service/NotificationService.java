package com.example.jujuassembly.domain.notification.service;

import com.example.jujuassembly.domain.notification.dto.NotificationResponse;
import com.example.jujuassembly.domain.notification.dto.NotificationsResponse;
import com.example.jujuassembly.domain.notification.entity.Notification;
import com.example.jujuassembly.domain.notification.repository.EmitterRepository;
import com.example.jujuassembly.domain.notification.repository.NotificationRepository;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

  private final EmitterRepository emitterRepository;
  private final NotificationRepository notificationRepository;

  public SseEmitter subscribe(User user, String lastEventId) {
    Long userId = user.getId();
    String id = userId + "_" + System.currentTimeMillis();
    SseEmitter emitter = emitterRepository.save(id, new SseEmitter(DEFAULT_TIMEOUT));

    emitter.onCompletion(() -> emitterRepository.deleteById(id));
    emitter.onTimeout(() -> emitterRepository.deleteById(id));

    // 503 에러를 방지하기 위한 더미 이벤트 전송
    sendToClient(emitter, id, "EventStream Created. [userId=" + userId + "]");

    // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
    if (!lastEventId.isEmpty()) {
      Map<String, Object> events = emitterRepository.findAllEventCacheStartWithId(
          String.valueOf(userId));
      events.entrySet().stream()
          .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
          .forEach(entry -> sendToClient(emitter, entry.getKey(), entry.getValue()));
    }
    return emitter;
  }


  private void sendToClient(SseEmitter emitter, String id, Object data) {
    try {
      emitter.send(SseEmitter.event()
          .id(id)
          .name("sse")
          .data(data));
    } catch (IOException exception) {
      emitterRepository.deleteById(id);
      log.error("SSE 연결 오류!", exception);
    }
  }

  @Transactional
  public void send(User user, Review review, String content) {
    Notification notification = createNotification(user, review, content);
    String id = String.valueOf(user.getId());
    notificationRepository.save(notification);
    Map<String, SseEmitter> sseEmitters = emitterRepository.findAllStartWithById(id);
    sseEmitters.forEach(
        (key, emitter) -> {
          emitterRepository.saveEventCache(key, notification);
          sendToClient(emitter, key, NotificationResponse.from(notification));
        }
    );
  }

  private Notification createNotification(User user, Review review, String content) {
    return Notification.builder()
        .user(user)
        .content(content)
        .review(review)
        .url("/reviews/" + review.getId())
        .isRead(false)
        .build();
  }



  @Transactional
  public NotificationsResponse findAllById(User user) {
    List<NotificationResponse> responses = notificationRepository.findAllByUserId(user.getId()).stream()
        .map(NotificationResponse::from)
        .collect(Collectors.toList());
    long unreadCount = responses.stream()
        .filter(notification -> !notification.isRead())
        .count();

    return NotificationsResponse.of(responses, unreadCount);
  }

  @Transactional
  public void readNotification(Long notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 알림입니다."));
    notification.read();
  }
}
