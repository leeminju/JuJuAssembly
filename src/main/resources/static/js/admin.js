const host = 'http://' + window.location.host;

$(document).ready(function () {
  authorizationCheck();
})

function authorizationCheck() {
  const auth = getToken();

  if (auth !== undefined && auth !== '') {
    $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
      jqXHR.setRequestHeader('Authorization', auth);
    });
  } else {
    window.location.href = '/';
    return;
  }

  //로그인한 회원 정보
  $.ajax({
    type: 'GET',
    url: `/v1/users/myprofile`,
    success: function (response) {
      if (response['data']['role'] === "USER") {
        window.location.href = '/';
      }
      $('#sign-in-btn').hide();
      $('#sign-up-btn').hide();
      $('#mypage').show();
      $('#logout-btn').show();
      $('#header_nickname').text(response['data']['nickname']);
    },
    error(error, status, request) {
      if (error['responseJSON']['data']) {
        alert(error['responseJSON']['data']);
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

  $.ajax({
    type: 'POST'
    , url: `/v1/users/logout`
    , success: function (response) {
      alert(response['msg']);
      CookieRemove();
      window.location.reload();
    }, error(error, status, request) {
      console.log(error);
    }
  });
}

function CookieRemove() {
  Cookies.remove('Authorization', {path: '/'});
}
