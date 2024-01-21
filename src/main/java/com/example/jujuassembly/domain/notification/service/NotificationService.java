package com.example.jujuassembly.domain.notification.service;

import com.example.jujuassembly.domain.notification.dto.NotificationRequestDto;
import com.example.jujuassembly.domain.notification.dto.NotificationResponseDto;
import com.example.jujuassembly.domain.notification.dto.NotificationsResponseDto;
import com.example.jujuassembly.domain.notification.entity.Notification;
import com.example.jujuassembly.domain.notification.repository.EmitterRepository;
import com.example.jujuassembly.domain.notification.repository.NotificationRepository;
import com.example.jujuassembly.domain.report.entity.Report;
import com.example.jujuassembly.domain.report.repository.ReportRepository;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.exception.ApiException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // SSE 연결의 기본 타임아웃 값 (60분)

  private final EmitterRepository emitterRepository;
  private final NotificationRepository notificationRepository;
  private final ReviewRepository reviewRepository;
  private final ReportRepository reportRepository;


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

  // 사용자에게 새 알림을 생성하고 저장한 후, 해당 사용자의 모든 SSE 연결에 이 알림을 전송
  @Transactional
  public void send(User user, String type, Long entityId, User actionUser) {
    // 새로운 createNotification 메서드를 사용하여 알림 생성
    Notification notification = createNotification(user, type, entityId, actionUser);

    // 알림 저장 및 전송 로직
    saveAndSendNotification(notification, user.getId());
  }

  private void saveAndSendNotification(Notification notification, Long userId) {
    // 생성된 알림 데이터베이스 저장
    notificationRepository.save(notification);

    // 사용자 ID를 기반으로 모든 SSE 연결 찾음
    Map<String, SseEmitter> sseEmitters = emitterRepository.findAllStartWithById(
        String.valueOf(userId));

    // 각 SSE 연결에 대해, 생성된 알림을 캐시에 저장 후 클라이언트에게 전송
    sseEmitters.forEach(
        (key, emitter) -> {
          emitterRepository.saveEventCache(key, notification);
          sendToClient(emitter, key, new NotificationResponseDto(notification));
        }
    );
  }

  // 알림 생성
  public Notification createNotification(User user, String entityType, Long entityId,
      User actionUser) {
    String url = "";
    String content = "";

    switch (entityType) {
      case "REVIEW":
        Optional<Review> optionalReview = reviewRepository.findById(entityId);
        if (optionalReview.isPresent()) {
          Review review = optionalReview.get();
          if (actionUser != null && review.getUser() != null) {
            url = "/productDetails?productId=" + review.getProduct().getId()
                + "&categoryId=" + review.getProduct().getCategory().getId();
            content = actionUser.getNickname() + "님이 " + review.getUser().getNickname()
                + "님의 리뷰에 좋아요를 눌렀습니다.";
          }
        }
        break;

      case "REPORT":
        Optional<Report> optionalReport = reportRepository.findById(entityId);
        if (optionalReport.isPresent()) {
          Report report = optionalReport.get();
          if (report.getUser() != null) {
            url = "/main/report";
            String statusString = report.getStatus().name().toString();

            switch (statusString) {
              case "PROCEEDING":
                content = report.getUser().getNickname() + "님의 제보가 아직 진행중입니다.";
                break;
              case "ADOPTED":
                content = report.getUser().getNickname() + "님의 제보가 채택되었습니다.";
                break;
              case "UN_ADOPTED":
                content = report.getUser().getNickname() + "님의 제보가 비채택되었습니다.";
                break;
              default:
                content = "알 수 없는 상태입니다.";
            }
          }
        }
        break;

      // 다른 케이스에 대한 처리...

      default:
        // 유효하지 않은 entityType인 경우
        return null;
    }

    NotificationRequestDto requestDto = NotificationRequestDto.builder()
        .user(user)
        .content(content)
        .url(url)
        .entityType(entityType)
        .entityId(entityId)
        .isRead(false)
        .build();

    return new Notification(requestDto);
  }


  // SseEmitter 객체 사용하여 클라이언트에게 이벤트 전송하는 메서드
  public void sendToClient(SseEmitter emitter, String id, Object data) {
    try {
      emitter.send(SseEmitter.event().id(id).name("sse").data(data));
    } catch (IOException exception) {
      emitterRepository.deleteById(id);
      log.error("SSE 연결 오류", exception);
    }
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

  // 알림 서비스 내에 알림 삭제 메서드 추가
  @Transactional
  public void deleteNotification(Long notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new ApiException("존재하지 않는 알림입니다.", HttpStatus.NOT_FOUND));
    notificationRepository.delete(notification);
  }


  // 알림 삭제
  public void deleteNotificationByEntity(String entityType, Long entityId) {
    List<Notification> notificationsToDelete = notificationRepository.findByEntityTypeAndEntityId(
        entityType, entityId);

    // 해당 항목(예: 리뷰 또는 제보)과 관련된 모든 알림을 삭제
    notificationRepository.deleteAll(notificationsToDelete);
  }
}
