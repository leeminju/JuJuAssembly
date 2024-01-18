$(document).ready(function () {
  // 알림 구독 버튼 클릭 이벤트 핸들러 추가
    initializeSSE();

  // 알림 보기 버튼 클릭 이벤트
  $('#notification-btn').click(function () {
    showModal();
    fetchNotifications();
  });
});

// SSE 구독을 위한 함수
function initializeSSE() {
  if (!!window.EventSource) {
    var source = new EventSource('/v1/notification/subscribe');

    source.onmessage = function(event) {
      var notification = JSON.parse(event.data);
      displayRealTimeNotification(notification);
      displayNotifications([notification]); // 알림 목록에 추가
    };

    source.onerror = function(error) {
      console.error('SSE 연결 오류:', error);
      // 필요에 따라 연결 재시도 로직 추가
    }
  } else {
    console.log("브라우저가 SSE를 지원하지 않습니다.");
  }
}

// 토스트 메시지를 표시하는 함수
function showToast(message) {
  var toast = $('<div class="toast">' + message + '</div>');
  $('body').append(toast);
  setTimeout(function() {
    toast.remove();
  }, 3000); // 3초 후 토스트 메시지 제거
}

// 실시간으로 수신된 알림을 화면에 표시하는 함수
function displayRealTimeNotification(notification) {
  showToast('새 알림: ' + notification.content);
}


// 서버로부터 알림 목록을 조회하는 함수
function fetchNotifications() {
  $.ajax({
    type: 'GET',
    url: '/v1/notification/notifications', // 서버의 알림 목록 엔드포인트
    success: function (response) {
      // 알림 목록 조회 성공 시 처리 로직
      console.log('알림 목록 조회 성공:', response);
      displayNotifications(response.data.notificationResponses); // 'data' 필드에 알림 목록이 있다고 가정
      displayUnreadCount(response.data.unreadCount); // 읽지 않은 알림 갯수 표시
    },
    error: function (xhr, status, error) {
      // 알림 목록 조회 실패 시 처리 로직
      console.error('알림 목록 조회 실패:', error);
      if (xhr.responseJSON && xhr.responseJSON.data) {
        alert(xhr.responseJSON.data);
      } else if (xhr.responseJSON && xhr.responseJSON.msg) {
        alert(xhr.responseJSON.msg);
      } else {
        alert('알림 목록을 조회할 수 없습니다.');
      }
    }
  });
}

// 화면에 알림 목록을 표시하는 함수
function displayNotifications(notifications) {
  var list = $('#notification-list');
  list.empty();

  if (notifications && notifications.length > 0) {
    $.each(notifications, function (i, notification) {
      var content = notification.content ? notification.content : "알림 내용 없음";
      var listItem = $('<li>' + content + '</li>');
      listItem.data('id', notification.id);
      listItem.data('url', notification.url);
      listItem.on('click', function () {
        markAsRead($(this).data('id'));
        window.location.href = $(this).data('url');
      });
      list.append(listItem);
    });
  } else {
    list.append('<li>알림이 없습니다.</li>');
  }
}

// 알림을 읽음 상태로 변경하는 함수
function markAsRead(notificationId) {
  $.ajax({
    url: '/v1/notification/notifications/' + notificationId, // 엔드포인트 URL 수정
    method: 'PATCH', // PATCH 요청
    success: function (response) {
      console.log('알림 읽음 처리 성공:', response);
      // 추가적으로 알림 목록을 업데이트하거나 사용자 인터페이스에 반영할 수 있습니다.
    },
    error: function (xhr, status, error) {
      console.error('알림 읽음 처리 실패:', error);
    }
  });
}

// 읽지 않은 알림의 개수를 표시하는 함수
function displayUnreadCount(unreadCount) {
  var unreadCountElement = $('#unread-count');
  unreadCountElement.text('읽지 않은 알림: ' + unreadCount);
}

// 알림 모달 창을 표시하는 함수
function showModal() {
  var modal = $('#notification-modal');
  modal.show();

  $('.close-btn').click(function () {
    modal.hide();
  });
}

// 모달 창 외부를 클릭하면 모달을 닫는 이벤트 핸들러
$(window).click(function (event) {
  var modal = $('#notification-modal');
  if ($(event.target).is(modal)) {
    modal.hide();
  }
});