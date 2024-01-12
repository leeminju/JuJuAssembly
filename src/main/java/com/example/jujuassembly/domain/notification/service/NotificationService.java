package com.example.jujuassembly.domain.notification.service;

import com.example.jujuassembly.domain.notification.dto.NotificationRequestDto;
import com.example.jujuassembly.domain.notification.dto.NotificationResponseDto;
import com.example.jujuassembly.domain.notification.dto.NotificationsResponseDto;
import com.example.jujuassembly.domain.notification.entity.Notification;
import com.example.jujuassembly.domain.notification.repository.EmitterRepository;
import com.example.jujuassembly.domain.notification.repository.NotificationRepository;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.exception.ApiException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // SSE 연결의 기본 타임아웃 값 (60분)

  private final EmitterRepository emitterRepository;
  private final NotificationRepository notificationRepository;

  // 사용자가 SSE를 통해 알림을 실시간으로 받을 수 있게 설정하는 메서드
  public SseEmitter subscribe(User user, String lastEventId) {
    Long userId = user.getId();

    // 사용자 ID와 현재 시간을 조합해 고유한 emitter 식별자 생성
    String id = userId + "_" + System.currentTimeMillis();

    SseEmitter emitter = emitterRepository.save(id, new SseEmitter(DEFAULT_TIMEOUT));

    // 연결이 완료되거나 타임아웃 되면 emitter를 레포지토리에서 제거
    emitter.onCompletion(() -> emitterRepository.deleteById(id));
    emitter.onTimeout(() -> emitterRepository.deleteById(id));

    // 503 에러를 방지하기 위한(SSE 연결을 유지하기 위한) 더미 이벤트 전송
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


  // SseEmitter 객체 사용하여 클라이언트에게 이벤트 전송하는 메서드
  private void sendToClient(SseEmitter emitter, String id, Object data) {
    try {
      emitter.send(SseEmitter.event().id(id).name("sse").data(data));
    } catch (IOException exception) {
      emitterRepository.deleteById(id);
      throw new ApiException("SSE 연결에 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // 사용자에게 새 알림을 생성하고 저장한 후, 해당 사용자의 모든 SSE 연결에 이 알림을 전송
  @Transactional
  public void send(User user, Review review, String content) {
    Notification notification = createNotification(user, review, content);
    String id = String.valueOf(user.getId());
    // 생성된 알림 데이터베이스 저장
    notificationRepository.save(notification);
    // 사용자 ID를 기반으로 모든 SSE 연결 찾음
    Map<String, SseEmitter> sseEmitters = emitterRepository.findAllStartWithById(id);

    // 각 SSE 연결에 대해, 생성된 알림을 캐시에 저장 후 클라이언트에게 전송
    sseEmitters.forEach(
        (key, emitter) -> {
          emitterRepository.saveEventCache(key, notification);
          sendToClient(emitter, key, new NotificationResponseDto(notification));
        }
    );
  }

  // 알림 생성
  private Notification createNotification(User user, Review review, String content) {
    String url = "/reviews/" + review.getId();
    NotificationRequestDto requestDto = new NotificationRequestDto(user, review, content, url,
        false);
    return new Notification(requestDto);
  }


  // 모든 알림 조회
  @Transactional
  public NotificationsResponseDto findAllById(User user) {
    List<NotificationResponseDto> responses = notificationRepository.findAllByUserId(user.getId())
        .stream()
        .map(NotificationResponseDto::new) // 생성자를 사용하여 객체 생성
        .collect(Collectors.toList());
    long unreadCount = responses.stream()
        .filter(notification -> !notification.isRead())
        .count();

    return new NotificationsResponseDto(responses, unreadCount);
  }

  // 지정된 ID의 알림을 찾아 '읽음' 상태로 변경
  @Transactional
  public void readNotification(Long notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new ApiException("존재하지 않는 알림입니다.", HttpStatus.NOT_FOUND));
    notification.read();
  }

  public void deleteNotificationByReviewAndUser(Review review, User user) {
    // NotificationRepository의 메서드를 호출하여 알림을 삭제
    notificationRepository.deleteByReviewAndUser(review, user);
  }
}
