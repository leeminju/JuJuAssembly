var source;

$(document).ready(function () {
  // 로그인 상태를 확인하는 로직 (예시: 쿠키에서 토큰 확인)
  var isLoggedIn = getToken(); // 사용자 로그인 상태 확인

  if (isLoggedIn) {
    initializeSSE(); // SSE 구독 초기화
    fetchNotifications(); // 기존 알림 목록 조회
  } else {
    $('#notification-count-badge').hide(); // 로그인되지 않았을 경우 알림 아이콘 숨김
  }
});

// SSE 구독을 위한 함수
function initializeSSE() {
  if (!!window.EventSource) {
    source = new EventSource('/v1/notification/subscribe');

    source.addEventListener("sse", function (event) {
      var data = JSON.parse(event.data);
      displayRealTimeNotification(data); // 실시간 알림 표시
      displayNotifications([data]); // 실시간 알림을 목록에 추가
    });

    source.onerror = function (error) {
      console.error('SSE 연결 오류:', error);
      // 필요에 따라 연결 재시도 로직 추가
    }
  } else {
    console.log("브라우저가 SSE를 지원하지 않습니다.");
  }
}


// 실시간으로 수신된 알림을 화면에 표시하는 함수
function displayRealTimeNotification(data) {
  // 브라우저 알림 허용 권한 확인 및 요청
  checkNotificationPermission().then(granted => {
    if (granted) {
      // 브라우저 알림 표시
      showNotification(data);
    }
  });
}

function checkNotificationPermission() {
  return new Promise((resolve, reject) => {
    if (Notification.permission === 'granted') {
      resolve(true);
    } else if (Notification.permission !== 'denied') {
      Notification.requestPermission().then(permission => {
        resolve(permission === 'granted');
      });
    } else {
      resolve(false);
    }
  });
}

function showNotification(data) {
  const notification = new Notification('새 알림', {
    body: data.content,
    icon: '/path/to/icon.png' // 알림 아이콘 경로 (옵션)
  });

  setTimeout(() => {
    notification.close();
  }, 10 * 1000);

  notification.onclick = () => {
    window.open(data.url, '_blank');
  };
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

  if (notifications.length === 0) {
    list.append('<li>새로운 알림이 없습니다.</li>');
  } else {
    notifications.forEach(function(notification) {
      var listItem = $('<li>').addClass('notification-item');
      var content = $('<span>').text(notification.content);

      if (notification.read) {
        content.css('color', 'grey'); // 읽은 알림의 스타일
        content.append(' (읽음)');
      } else {
        content.css('color', 'black'); // 안 읽은 알림의 스타일
      }

      // 알림 클릭 이벤트 핸들러
      content.click(function() {
        markAsRead(notification.id);
        window.location.href = notification.url; // 해당 알림의 URL로 이동
      });

      listItem.append(content);

      // 삭제 버튼 (옵션)
      var deleteButton = $('<button>').text('delete').css({
        'border-radius': '15px',
        'margin-left': '200px',
        'border-style': 'none',
        'font-size': '15px',
        'font-variant': 'all-small-caps',
        'cursor': 'pointer'
      }).click(function() {
        deleteNotification(notification.id);
      });

      listItem.append(deleteButton);

      list.append(listItem);
    });
  }
}


// 알림을 읽음 상태로 변경하는 함수
function markAsRead(notificationId) {
  $.ajax({
    url: '/v1/notification/notifications/' + notificationId, // 엔드포인트 URL 수정
    method: 'PATCH', // PATCH 요청
    success: function (response) {
      console.log('알림 읽음 처리 성공:', response);
      // 추가적으로 알림 목록을 업데이트하거나 사용자 인터페이스에 반영
      fetchNotifications();
    },
    error: function (xhr, status, error) {
      console.error('알림 읽음 처리 실패:', error);
    }
  });
}

// 알림 삭제를 요청하는 함수
function deleteNotification(notificationId) {
  $.ajax({
    url: '/v1/notification/notifications/' + notificationId,
    type: 'DELETE',
    success: function(response) {
      console.log('알림 삭제 성공:', response);
      fetchNotifications(); // 알림 목록을 다시 불러옴
    },
    error: function(xhr, status, error) {
      console.error('알림 삭제 실패:', error);
    }
  });
}

// 읽지 않은 알림의 개수를 표시하는 함수
function displayUnreadCount(unreadCount) {
  var unreadCountBadge = $('#notification-count-badge'); // 알림 버튼 옆의 배지
  var modalUnreadCountElement = $('#unread-count'); // 모달 창 내의 텍스트 요소

  if(unreadCount > 0) {
    unreadCountBadge.text(unreadCount); // 버튼 옆의 배지에 숫자만 표시
    modalUnreadCountElement.text("읽지 않은 알림: " + unreadCount); // 모달 창 내의 텍스트를 '읽지 않은 알림: 숫자'로 업데이트
    unreadCountBadge.show(); // 숨겨져 있었다면 배지를 보이게 처리
  } else {
    unreadCountBadge.hide(); // 알림이 0개일 때는 배지를 숨김
    modalUnreadCountElement.text("읽지 않은 알림이 없습니다."); // 모달 창 내 텍스트를 '읽지 않은 알림이 없습니다.'로 업데이트
  }
}