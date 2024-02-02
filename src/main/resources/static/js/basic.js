const host = 'http://' + window.location.host;
let role = null;
let myId = null;
let nickname = null;
let email = null;
let firstId = null;
let secondId = null;
let loginId = null;
let profileImage = null;

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
    async: false,
    success: function (response) {
      let user = response['data'];
      role = user['role'];
      myId = user['id'];
      nickname = user['nickname'];
      profileImage = user['image'];
      loginId = user['loginId'];
      email = user['email'];
      firstId = user['firstPreferredCategoryId'];
      secondId = user['secondPreferredCategoryId'];

      if (role === "ADMIN") {
        $('#admin_btn').show();
      }

      $('#sign-in-btn').hide();
      $('#sign-up-btn').hide();
      $('#mypage').show();
      $('#logout-btn').show();
      $('#header_nickname').text(nickname);
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

  $.ajax({
    type: 'POST',
    url: `/v1/users/logout`,
    success: function (response) {
      msg = response['msg'];
      alert(msg)
      window.location.href = "/";
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

function CookieRemove() {
  Cookies.remove('Authorization', {path: '/'});
}
