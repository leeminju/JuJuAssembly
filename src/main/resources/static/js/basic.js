const host = 'http://' + window.location.host;

$(document).ready(function () {
  authorizationCheck();
})

function showAdminMenu() {
  $('#adminMenu').toggle();
}

function authorizationCheck() {
  const auth = getToken();

  if (auth !== undefined && auth !== '') {
    $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
      jqXHR.setRequestHeader('Authorization', auth);
    });
  } else {
    $('#sign-in-btn').show();
    $('#sign-up-btn').show();
    $('#mypage').hide();
    $('#logout-btn').hide();
    $('#admin_btn').hide();
    return;
  }

  //로그인한 회원 정보
  $.ajax({
    type: 'GET',
    url: `/v1/users/myprofile`,
    success: function (response) {
      role = response['data']['role'];
      if (response['data']['role'] === "ADMIN") {
        $('#admin_btn').show();
      }

      $('#sign-in-btn').hide();
      $('#sign-up-btn').hide();
      $('#mypage').show();
      $('#logout-btn').show();
      $('#header_nickname').text(response['data']['nickname']);
    },
    error(error, status, request) {
      if (error['responseJSON']['data']) {
        alert(JSON.stringify(error['responseJSON']['data']));
      } else {
        alert(error['responseJSON']['msg']);
      }
    }
  });
}

function getToken() {
  let auth = Cookies.get('Authorization');

  if (auth === undefined) {
    return '';
  }

  return auth;
}

function logout() {
  CookieRemove();
  alert("로그아웃");
  // // SSE 연결 종료
  // if (source) {
  //   source.close();
  //   source = null; // 참조 제거
  // }

  // $.ajax({
  //   type: 'POST'
  //   , url: `/v1/users/logout`
  //   , success: function (response) {
  //     alert(response['msg']);
  //     CookieRemove();
  //     window.location.reload();
  //   }, error(error, status, request) {
  //     console.log(error);
  //   }
  // });
}

function CookieRemove() {
  Cookies.remove('Authorization', {path: '/'});
}
