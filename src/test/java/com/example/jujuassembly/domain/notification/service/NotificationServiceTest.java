package com.example.jujuassembly.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.notification.dto.NotificationRequestDto;
import com.example.jujuassembly.domain.notification.dto.NotificationResponseDto;
import com.example.jujuassembly.domain.notification.dto.NotificationsResponseDto;
import com.example.jujuassembly.domain.notification.entity.Notification;
import com.example.jujuassembly.domain.notification.repository.EmitterRepository;
import com.example.jujuassembly.domain.notification.repository.NotificationRepository;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.report.entity.Report;
import com.example.jujuassembly.domain.report.repository.ReportRepository;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.exception.ApiException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NotificationServiceTest {

  @Mock
  SseEmitter emitter1;
  @Mock
  SseEmitter emitter2;
  @Mock
  EmitterRepository emitterRepository;
  @Mock
  NotificationRepository notificationRepository;
  @Mock
  ReviewRepository reviewRepository;
  @Mock
  ReportRepository reportRepository;
  @Mock
  User user;
  @InjectMocks
  NotificationService notificationService;


  @Test
  @DisplayName("사용자가 SSE를 통해 알림을 실시간으로 받을 수 있게 설정하는 메서드의 테스트")
  void subscribeTest() {
    Long userId = 1L;
    String lastEventId = "lastEventId";

    // SseEmitter를 모의 객체로 생성
    SseEmitter mockEmitter = mock(SseEmitter.class);

    // 사용자 ID 설정
    when(user.getId()).thenReturn(userId);

    // 모의 객체를 반환하도록 설정
    when(emitterRepository.save(anyString(), any(SseEmitter.class)))
        .thenReturn(mockEmitter);

    // subscribe 메서드 호출
    SseEmitter resultEmitter = notificationService.subscribe(user, lastEventId);

    // 결과 검증
    assertNotNull(resultEmitter);
    verify(emitterRepository).save(anyString(), any(SseEmitter.class));

    // SseEmitter에 전달된 이벤트 캡쳐
    ArgumentCaptor<SseEventBuilder> captor = ArgumentCaptor.forClass(SseEventBuilder.class);

    // Dummy Event 전송 로직 검증
    String expectedDummyEventMessage = "EventStream Created. [userId=" + userId + "]";
    try {
      verify(mockEmitter).send(captor.capture());
      SseEmitter.SseEventBuilder capturedEventBuilder = captor.getValue();

      // build() 메서드로부터 실제 데이터 세트 얻기
      Set<DataWithMediaType> actualDataWithMediaTypeSet = capturedEventBuilder.build();

      // actualDataWithMediaTypeSet 내의 데이터 중 하나가 expectedDummyEventMessage와 일치하는지 검사
      boolean isExpectedMessagePresent = actualDataWithMediaTypeSet.stream()
          .anyMatch(dataWithMediaType -> {
            Object data = dataWithMediaType.getData(); // 실제 데이터 객체
            // 이 부분에서 data 객체의 내용을 검사하여 expectedDummyEventMessage와 일치하는지 확인
            return data.toString().equals(expectedDummyEventMessage);
          });

      assertTrue(isExpectedMessagePresent);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // 미수신 이벤트 전송 로직 검증
    Map<String, Object> mockEvents = new HashMap<>();

    // 테스트 데이터 생성
    mockEvents.put("event1", "data1");
    mockEvents.put("event2", "data2");

    when(emitterRepository.findAllEventCacheStartWithId(eq(String.valueOf(userId))))
        .thenReturn(mockEvents);

    notificationService.subscribe(user, lastEventId);

    // 미수신 이벤트가 적절히 전송되었는지 검증
    mockEvents.entrySet().stream()
        .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
        .forEach(entry -> {
          try {
            verify(mockEmitter).send(captor.capture());
            SseEventBuilder event = captor.getValue();
            // 각 이벤트의 데이터가 모의 데이터와 일치하는지 검증
            assertEquals(entry.getValue(), event.build().getClass());
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  @Test
  @DisplayName("SseEmitter를 통해 클라이언트에게 이벤트를 전송 성공 테스트")
  void testSendToClientSuccess() throws IOException {
    // Arrange
    SseEmitter emitter = mock(SseEmitter.class);
    String id = "1";
    Object data = "Test data";

    // Act
    notificationService.sendToClient(emitter, id, data);

    // Assert
    verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    verify(emitterRepository, never()).deleteById(anyString());
  }


  @Test
  @DisplayName("사용자에게 새 알림을 생성하고 저장한 후, 해당 사용자의 모든 SSE 연결에 이 알림을 전송하는 테스트")
  void send() throws IOException {
    // 테스트에 필요한 객체 생성
    User user = User.builder().id(1L).build();
    User actionUser = User.builder().id(20L).build();
    Category category = Category.builder().id(10L).name("TestCategory").build();
    Product product = Product.builder().id(100L).category(category).name("TestProduct").build();
    Notification notification = Notification.builder()
        .id(1L)
        .user(user)
        .content("Test Content 1")
        .url("http://jujuAssembly.com/1")
        .isRead(false)
        .entityType("REVIEW")
        .entityId(100L)
        .build();

    // notificationRepository.save() 메서드의 동작 설정
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    // emitterRepository.findAllStartWithById() 메서드의 동작 설정
    Map<String, SseEmitter> sseEmittersForUser1 = new HashMap<>();
    sseEmittersForUser1.put("1", emitter1);
    sseEmittersForUser1.put("2", emitter2);

    // "1" 사용자에게 연결된 SSE 엔터티 설정
    when(emitterRepository.findAllStartWithById(eq("1"))).thenReturn(sseEmittersForUser1);
    // "2" 사용자에게 연결된 SSE 엔터티 설정 (비어있음)
    when(emitterRepository.findAllStartWithById(eq("2"))).thenReturn(Collections.emptyMap());

    // 테스트할 메서드 호출
    notificationService.send(user, "REVIEW", 100L, actionUser);

    // notificationRepository.save() 메서드가 호출되었는지 검증
    verify(notificationRepository, times(1)).save(any(Notification.class));

    // emitterRepository의 메서드 호출 검증
    verify(emitterRepository, times(1)).findAllStartWithById(eq("1"));
    verify(emitterRepository, times(2)).saveEventCache(anyString(), any(Notification.class));

    // emitter1과 emitter2에 대해 한 번씩 전송 검증
    verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
  }


  @Test
  @DisplayName("알림 생성 테스트")
  void createNotificationTest() {
    // 가상 데이터 생성
    User user = User.builder().id(1L).nickname("UserNickname").build();
    User actionUser = User.builder().id(2L).nickname("ActionUserNickname").build();

    Category category = Category.builder().id(10L).build();
    Product product = Product.builder().id(100L).category(category).build();
    Review review = Review.builder().id(1000L).product(product).user(user).build();

    // reviewRepository 및 reportRepository의 모의 동작 설정
    when(reviewRepository.findById(eq(review.getId()))).thenReturn(Optional.of(review));

    // createNotification 메서드 호출
    Notification notification = notificationService.createNotification(user, "REVIEW",
        review.getId(), actionUser);

    // 검증
    assertNotNull(notification);
    assertEquals(user, notification.getUser());
    assertEquals("ActionUserNickname님이 UserNickname님의 리뷰에 좋아요를 눌렀습니다.", notification.getContent());
    assertEquals("/productDetails?productId=100&categoryId=10", notification.getUrl());
    assertFalse(notification.isRead());
  }

  @Test
  @DisplayName("모든 알림 조회 테스트")
  void findAllByIdTest() {
    // 가상 데이터 생성
    User user = User.builder()
        .id(1L)
        .build();

    Category category = Category.builder()
        .id(10L)
        .build();

    Product product = Product.builder()
        .id(100L)
        .category(category)
        .build();

    Review review = Review.builder()
        .id(1000L)
        .product(product)
        .build();
    String content = "Test Notification Content";

    // NotificationRequestDto 생성 부분 수정
    NotificationRequestDto requestDto = NotificationRequestDto.builder()
        .user(user)
        .content(content)
        .url("url")
        .entityType("entityType") // 추가
        .entityId(1L) // 추가
        .isRead(false)
        .build();

    // NotificationRequestDto를 사용하여 알림 생성
    Notification notification1 = new Notification(requestDto);
    Notification notification2 = new Notification(requestDto);
    Notification notification3 = new Notification(requestDto);

    // Mock 객체 설정: NotificationRepository의 findAllByUserId 메서드 호출 시 알림 목록 반환
    List<Notification> notifications = Arrays.asList(notification1, notification2, notification3);
    when(notificationRepository.findAllByUserId(user.getId())).thenReturn(notifications);

    // 테스트 대상 메서드 호출
    NotificationsResponseDto responseDto = notificationService.findAllById(user);

    // 결과 검증
    assertEquals(notifications.size(),
        responseDto.getNotificationResponses().size()); // 알림 목록 크기가 일치해야 함
    assertEquals(3, responseDto.getUnreadCount()); // 읽지 않은 알림 수는 0이어야 함

    // UserRepository의 findAllByUserId 메서드가 1번 호출되었는지 검증
    verify(notificationRepository, times(1)).findAllByUserId(user.getId());

    // Mock 객체를 통해 반환한 알림 목록과 ResponseDto의 알림 목록을 비교하여 확인
    List<NotificationResponseDto> responseNotifications = responseDto.getNotificationResponses();
    assertEquals(notifications.size(), responseNotifications.size());

    for (int i = 0; i < notifications.size(); i++) {
      Notification notification = notifications.get(i);
      NotificationResponseDto responseNotification = responseNotifications.get(i);

      //  알림 내용(content)이 일치하는 지 확인
      assertEquals(notification.getContent(), responseNotification.getContent());
    }
  }

  @Test
  @DisplayName("지정된 ID의 알림을 찾아 '읽음' 상태로 변경 테스트")
  void readNotificationTest() {
    // 가상 데이터 생성
    Long notificationId = 1L;
    Notification notification = Notification.builder()
        .id(notificationId)
        .isRead(false) // 초기 읽음 상태 설정 (false)
        .build();

    // notificationRepository에 저장
    notificationRepository.save(notification);

    // NotificationRepository의 findById 메서드가 가상의 알림 객체를 반환하도록 설정
    when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

    // NotificationRepository의 save 메서드가 가상의 알림 객체를 반환하도록 설정
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    // 메서드 실행
    notificationService.readNotification(notificationId);

    // 상태 변경 확인
    assertTrue(notification.isRead());

    // Notification 객체의 상태가 변경되었는지 검증
    verify(notificationRepository).findById(notificationId);

    // NotificationRepository의 save 메서드 호출 여부 확인
    // 상태가 변경된 알림 객체가 저장되었는지 검증
    verify(notificationRepository).save(notification);
  }


  @Test
  @DisplayName("특정 엔티티와 관련된 모든 알림 삭제 테스트")
  void deleteNotificationByEntityTest() {
    // 가상 데이터 생성
    String entityType = "REVIEW";
    Long entityId = 100L;

    User mockUser = User.builder().id(1L).nickname("TestUser").build();
    Notification mockNotification1 = Notification.builder()
        .id(1L)
        .user(mockUser)
        .content("Test Content 1")
        .url("http://jujuAssembly.com/1")
        .isRead(false)
        .entityType("REVIEW")
        .entityId(100L)
        .build();

    Notification mockNotification2 = Notification.builder()
        .id(2L)
        .user(mockUser)
        .content("Test Content 2")
        .url("http://jujuAssembly.com/2")
        .isRead(false)
        .entityType("REPORT")
        .entityId(100L)
        .build();

    List<Notification> mockNotifications = List.of(mockNotification1, mockNotification2);

    // NotificationRepository의 findByEntityTypeAndEntityId 메서드가 mockNotifications를 반환하도록 설정
    when(notificationRepository.findByEntityTypeAndEntityId(entityType, entityId)).thenReturn(
        mockNotifications);

    // NotificationRepository의 deleteAll 메서드가 아무 것도 하지 않도록 설정
    doNothing().when(notificationRepository).deleteAll(mockNotifications);

    // 테스트 대상 메서드 호출
    notificationService.deleteNotificationByEntity(entityType, entityId);

    // NotificationRepository의 findByEntityTypeAndEntityId 메서드와 deleteAll 메서드가 각각 호출되었는지 검증
    verify(notificationRepository, times(1)).findByEntityTypeAndEntityId(entityType, entityId);
    verify(notificationRepository, times(1)).deleteAll(mockNotifications);
  }
}
