var source;

$(document).ready(function () {
  // 로그인 상태를 확인하는 로직
  var isLoggedIn = getToken(); // 사용자 로그인 상태 확인

  if (isLoggedIn) {
    // 로그인 상태라면 필요한 초기화 수행
    initializeSSE(); // SSE 구독 초기화
    fetchNotifications(); // 기존 알림 목록 조회
  } else {
    // 로그인 상태가 아니라면 알림 아이콘 숨김
    $('#notification-count-badge').hide();
  }

});

// SSE 구독을 위한 함수
function initializeSSE() {
  if (!!window.EventSource) {
    // 기존의 EventSource 인스턴스가 있다면 닫고 초기화
    // if (source) {
    //   source.close();
    //   source = null;
    // }

    source = new EventSource('/v1/notification/subscribe');

    source.addEventListener("sse", function (event) {
      const data = JSON.parse(event.data);

      // 더미 데이터인 경우 무시
      if (data.toString().includes("EventStream Created")) {
        return;
      }

      displayRealTimeNotification(data); // 실시간 알림 표시
      displayNotifications([data]); // 실시간 알림을 목록에 추가
      fetchNotifications(); // 새 알림으로 목록 및 뱃지 업데이트
    });

    source.onerror = function (error) {
      console.error('SSE 연결 오류:', error);
      // 연결 오류 발생 시 기존 인스턴스 정리
      if (source) {
        source.close();
        source = null;
      }
      // 필요한 경우 재연결 로직
      setTimeout(initializeSSE, 5000); // 5초 후 재연결 시도
    }
  } else {
    console.log("브라우저가 SSE를 지원하지 않습니다.");
  }
}

// // 서버로부터 새로운 알림 목록을 가져와 업데이트하는 함수
// function updateNotificationsListAndBadge() {
//   fetchNotifications(); // 서버로부터 최신 알림 목록을 가져옴
// }

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
    body: data.content
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
      // 부모 요소에 relative positioning 추가
      var listItem = $('<li>').addClass('notification-item').css('position', 'relative');
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
        'position': 'absolute',  // 버튼을 listItem에 상대적으로 위치시킴
        'right': '10px',         // 오른쪽 여백 설정
        'top': '50%',            // 버튼을 상위 요소의 중앙에 위치시킴
        'transform': 'translateY(-50%)', // 버튼을 정확히 중앙에 위치시키기 위해 Y축 기준으로 -50% 조정
        'border-radius': '30px',
        'border-style': 'none',
        'font-size': '15px',
        'font-variant': 'all-small-caps',
        'cursor': 'pointer'
      }).click(function() {
        deleteNotification(notification.id);
      });

      listItem.append(deleteButton);

      list.append(listItem);

      list.prepend(listItem); // 새 알림을 목록의 맨 위에 추가
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
  var unreadCountBadge = $('#notification-count-badge');
  var modalUnreadCountElement = $('#unread-count');

  if(unreadCount > 0) {
    unreadCountBadge.text(unreadCount);
    modalUnreadCountElement.text("읽지 않은 알림: " + unreadCount);
    unreadCountBadge.show();
  } else {
    unreadCountBadge.hide();
    modalUnreadCountElement.text("읽지 않은 알림이 없습니다.");
  }
}