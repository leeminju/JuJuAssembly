package com.example.jujuassembly.domain.notification.controller;

import com.example.jujuassembly.domain.notification.dto.NotificationsResponseDto;
import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notification")
public class NotificationController {

  private final NotificationService notificationService;

  /**
   * 로그인 한 유저의 Server-Sent Events(SSE) 연결을 관리합니다. SseEmitter를 사용하여 클라이언트에게 비동기적으로 이벤트를 보냅니다.
   *
   * @param userDetails 현재 인증된 사용자의 세부 정보
   * @param lastEventId 마지막으로 수신한 이벤트의 ID (필수는 아님)
   * @return SSE 연결을 위한 SseEmitter 객체
   */
  @GetMapping(value = "/subscribe", produces = "text/event-stream")
  public SseEmitter subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam(value = "lastEventId", required = false, defaultValue = "") String lastEventId) {
    // lastEventId : 마지막 이벤트 ID, 클라이언트가 마지막으로 수신한 이벤트를 식별하는데 사용 (필수는 아님)
    return notificationService.subscribe(userDetails.getUser(), lastEventId);
  }

  /**
   * 로그인한 사용자의 모든 알림을 조회합니다. ResponseEntity를 통해 알림 데이터와 HTTP 상태 코드를 함께 반환합니다.
   *
   * @param userDetails 현재 인증된 사용자의 세부 정보
   * @return 사용자의 모든 알림을 포함하는 ApiResponse 객체
   */
  @GetMapping("/notifications")
  public ResponseEntity<ApiResponse> notifications(
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    NotificationsResponseDto responseDto = notificationService.findAllById(userDetails.getUser());
    return ResponseEntity.ok()
        .body(new ApiResponse<>("알림 조회에 성공하였습니다.", HttpStatus.OK.value(), responseDto));
  }

  /**
   * 지정된 알림의 읽음 상태를 변경합니다. 성공적으로 변경되면 상태 메시지와 함께 HTTP 상태 코드를 반환합니다.
   *
   * @param notificationId 변경할 알림의 ID
   * @return 알림 상태 변경에 대한 ApiResponse 객체
   */
  @PatchMapping("/notifications/{notificationId}")
  public ResponseEntity<ApiResponse> readNotification(@PathVariable Long notificationId) {
    notificationService.readNotification(notificationId);
    return ResponseEntity.ok()
        .body(new ApiResponse<>("알림 읽음 상태 변경에 성공하였습니다.", HttpStatus.OK.value()));
  }

}
