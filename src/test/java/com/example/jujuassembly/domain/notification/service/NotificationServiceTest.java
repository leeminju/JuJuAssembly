package com.example.jujuassembly.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
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
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.exception.ApiException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@AutoConfigureMockMvc // MockMvc를 사용하기 위한 설정
public class NotificationServiceTest {

  @Mock
  SseEmitter emitter;
  @Mock
  EmitterRepository emitterRepository;
  @Mock
  NotificationRepository notificationRepository;

  @Mock
  User user;
  @InjectMocks
  NotificationService notificationService;

//  @BeforeEach
//  public void setUp() {
//    // Mockito 어노테이션을 사용한 모의 객체 초기화
//    MockitoAnnotations.openMocks(this);
//
//  }

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
    NotificationService notificationService = new NotificationService(emitterRepository, notificationRepository);
    notificationService.sendToClient(emitter, id, data);

    // Assert
    verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    verify(emitterRepository, never()).deleteById(anyString());
  }

//  @Test
//  @DisplayName("SseEmitter를 통해 클라이언트에게 예외 발생시키고 예외 처리하는 테스트")
//  public void testSendToClientWithException() throws IOException {
//    // Arrange
//    SseEmitter emitter = mock(SseEmitter.class);
//    String id = "1";
//    Object data = "Test data";
//
//    // Mock 객체의 동작 설정: emitter.send() 메서드가 예외를 던지도록 설정
//    doThrow(new IOException("Test exception")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));
//
//    // EmitterRepository 목 객체 생성
//    EmitterRepository emitterRepository = mock(EmitterRepository.class);
//
//    // NotificationService 객체 생성 및 EmitterRepository 주입
//    NotificationService notificationService = new NotificationService(emitterRepository, notificationRepository);
//
//    // Act & Assert: ApiException이 발생하는지 확인
//    ApiException exception = assertThrows(ApiException.class, () -> {
//      notificationService.sendToClient(emitter, id, data);
//    });
//
//    // ApiException이 발생한 경우에만 메시지 검증 수행
//    if (exception != null) {
//      // ApiException의 메시지 검증
//      String expectedErrorMessage = "SSE 연결에 오류가 발생하였습니다.";
//      String actualErrorMessage = exception.getMessage();
//      assertNotNull(actualErrorMessage);
//      assertTrue(actualErrorMessage.contains(expectedErrorMessage));
//    }
//
//    // ApiException이 발생하면 아래 코드가 실행되지 않을 것입니다.
//    // emitterRepository.deleteById()가 호출되었는지 검증
//    verify(emitterRepository, times(1)).deleteById(eq(id));
//  }



//@Test
//@DisplayName("사용자에게 새 알림을 생성하고 저장한 후, 해당 사용자의 모든 SSE 연결에 이 알림을 전송하는 테스트")
//void send() {
//  // User 객체 생성
//  User user = User.builder()
//      .id(1L)
//      .build();
//
//  // Category 객체 생성
//  Category category = Category.builder()
//      .id(10L)
//      .name("TestCategory")
//      .build();
//
//  // Product 객체 생성
//  Product product = Product.builder()
//      .id(100L)
//      .category(category) // Category 객체 연결
//      .name("TestProduct")
//      .build();
//
//  // Review 객체 생성
//  Review review = Review.builder()
//      .id(1000L)
//      .product(product) // Product 객체 연결
//      .build();
//
//  String content = "New notification content";
//
//  Notification notification = Notification.builder()
//      .user(user)
//      .review(review)
//      .content(content)
//      .build();
//
//  // notificationRepository.save() 메서드의 동작 설정
//  when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
//
//  // emitterRepository.findAllStartWithById() 메서드의 동작 설정
//  Map<String, SseEmitter> sseEmittersForUser1 = new HashMap<>();
//  SseEmitter emitter1 = new SseEmitter();
//  SseEmitter emitter2 = new SseEmitter();
//  sseEmittersForUser1.put("1", emitter1);
//  sseEmittersForUser1.put("2", emitter2);
//
//  when(emitterRepository.findAllStartWithById(eq("1"))).thenReturn(sseEmittersForUser1);
//  when(emitterRepository.findAllStartWithById(eq("2"))).thenReturn(Collections.emptyMap());
//
//  // 테스트할 메서드 호출
//  notificationService.send(user, review, content);
//
//  // notificationRepository.save() 메서드가 호출되었는지 검증
//  verify(notificationRepository, times(1)).save(any(Notification.class));
//
//  // emitterRepository.saveEventCache() 메서드가 각 SSE 연결에 대해 호출되었는지 검증
//  verify(emitterRepository, times(1)).saveEventCache(eq("1"), eq(notification));
//  verify(emitterRepository, times(1)).saveEventCache(eq("2"), eq(notification));
//
//  // sendToClient() 메서드가 각 SSE 연결에 대해 호출되었는지 검증
//  verify(notificationService, times(1)).sendToClient(eq(emitter1), eq("1"), any(NotificationResponseDto.class));
//  verify(notificationService, times(1)).sendToClient(eq(emitter2), eq("2"), any(NotificationResponseDto.class));
//}




  @Test
  @DisplayName("알림 생성 테스트")
  void createNotificationTest() {
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

    // createNotification 메서드 호출
    Notification notification = notificationService.createNotification(user, review, content);

    // 반환된 알림 객체의 필드 검증
    assertNotNull(notification);
    assertEquals(user, notification.getUser());
    assertEquals(review, notification.getReview());
    assertEquals(content, notification.getContent());

    // URL 문자열 검증
    String expectedUrl = "/v1/categories/10/products/100#review-1000";
    assertEquals(expectedUrl, notification.getUrl());

    // isRead 필드 검증 (기본값은 false 여야 함)
    assertFalse(notification.isRead());
  }
//
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

  // 알림 생성을 위한 NotificationRequestDto 생성
  NotificationRequestDto requestDto = new NotificationRequestDto(user, review, content, "URL", true);

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
  assertEquals(notifications.size(), responseDto.getNotificationResponses().size()); // 알림 목록 크기가 일치해야 함
  assertEquals(0, responseDto.getUnreadCount()); // 읽지 않은 알림 수는 0이어야 함

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
  @DisplayName("특정 사용자가 특정 리뷰에 대해 수행한 행동에 대한 삭제 테스트")
  void deleteNotificationByReviewAndUserTest() {
    // 가상 데이터 생성
    User user = User.builder().id(1L).build();
    Review review = Review.builder().id(100L).build();

    // NotificationRepository의 deleteByReviewAndUser 메서드가 아무 것도 하지 않도록 설정
    doNothing().when(notificationRepository).deleteByReviewAndUser(review, user);

    // 테스트 대상 메서드 호출
    assertDoesNotThrow(() -> notificationService.deleteNotificationByReviewAndUser(review, user));

    // NotificationRepository의 deleteByReviewAndUser 메서드가 1번 호출되었는지 검증
    verify(notificationRepository, times(1)).deleteByReviewAndUser(review, user);
  }

  @Test
  @DisplayName("리뷰 삭제 시 해당 리뷰 관련 모든 알림 삭제 테스트")
  void deleteNotificationsByReviewTest() {
    // 가상 데이터 생성
    Review review = Review.builder().id(100L).build();

    // 테스트 대상 메서드 호출
    assertDoesNotThrow(() -> notificationService.deleteNotificationsByReview(review));

    // NotificationRepository의 deleteByReview 메서드가 1번 호출되었는지 검증
    verify(notificationRepository, times(1)).deleteByReview(review);
  }
}