package com.example.jujuassembly.domain.notification.controller;

import com.example.jujuassembly.domain.notification.dto.NotificationsResponse;
import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  /**
   * @title 로그인 한 유저 sse 연결
   */
  @GetMapping(value = "/subscribe", produces = "text/event-stream")
  public SseEmitter subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam(value = "lastEventId", required = false, defaultValue = "") String lastEventId) {
    return notificationService.subscribe(userDetails.getUser(), lastEventId);
  }

  /**
   * @title 로그인 한 유저의 모든 알림 조회
   */
  @GetMapping("/notifications")
  public ResponseEntity<NotificationsResponse> notifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
    return ResponseEntity.ok().body(notificationService.findAllById(userDetails.getUser()));
  }

  /**
   * @title 알림 읽음 상태 변경
   */
  @PatchMapping("/notifications/{id}")
  public ResponseEntity<Void> readNotification(@PathVariable Long notificationId) {
    notificationService.readNotification(notificationId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
