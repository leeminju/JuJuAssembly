async function getCategoryName(id) {
  try {
    const response = await $.ajax({
      type: 'GET',
      url: `/v1/categories/${id}`
    });

    let category = response['data'];
    let name = category['name'];
    console.log(name);
    return name;
  } catch (error) {
    alert(error['responseJSON']['msg']);
    return null;
  }
}

async function getUserInfo() {
  $('#loginId').text(loginId);
  $('#nickname').text(nickname);
  $('#email').text(email);

  if (firstId != null) {
    let firstName = await getCategoryName(firstId);
    $('#firstPreferredCategory').text(firstName);
  }
  if (secondId != null) {
    let secondName = await getCategoryName(secondId);
    $('#secondPreferredCategory').text(secondName);
  }

  if (!profileImage) {
    profileImage = "/images/default_user_image.png"
  }
  $('#image').attr('src', profileImage);
}

$(document).ready(async function () {
  if (myId == null) {
    alert("로그인이 필요한 서비스 입니다.");
    window.location.href = "/login";
  } else {
    getUserInfo();
  }
  getCategory();

  $('#image').click(function () {
    $('#imageInput').click();
  });

  // 파일 입력 창의 내용이 변경되면 호출되는 함수
  $('#imageInput').change(function (event) {
    // 선택한 파일 정보 가져오기
    const selectedFile = event.target.files[0];

    if (selectedFile) {
      const formData = new FormData();
      formData.append('image', selectedFile);

      $.ajax({
        type: 'POST',
        url: `/v1/users/${myId}`,
        data: formData,
        contentType: false,
        processData: false,
        success: function (response) {
          alert(response.msg);
          $('#image').attr('src', URL.createObjectURL(selectedFile));
          window.location.reload();
        },
        error(error, status, request) {
          alert(error['responseJSON']['msg'])
          window.location.reload();
        }
      });

    }
  });
});

function getCategory() {
  $.ajax({
    type: "GET",
    url: `/v1/categories`,
    success: function (response) {
      let categories = response['data'];
      console.log(categories);
      for (let i = 0; i < categories.length; i++) {
        let category = categories[i];
        let id = category['id'];
        let name = category['name'];
        let html = `<option value=${id}>${name}</option>`
        $('#firstPreferredCategoryId').append(html);
        $('#secondPreferredCategoryId').append(html);
      }
    },
    error(error, status, request) {
    }
  });

}

function showEditArea() {
  $('#edit-area').show();
  $('#show-area').hide();
}

function showOriginal() {
  $('#edit-area').hide();
  $('#show-area').show();
}

function XSSCheck(str, level) {
  if (level == undefined || level == 0) {
    str = str.replace(/\<|\>|\"|\'|\%|\;|\(|\)|\&|\+|\-/g, "");
  } else if (level != undefined && level == 1) {
    str = str.replace(/\</g, "&lt;");
    str = str.replace(/\>/g, "&gt;");
  }
  return str;
}

function updateProfile() {
  let nickname = $('#edit-nickname').val();
  nickname = XSSCheck(nickname, 0);
  let currentPassword = $('#current_password').val();
  currentPassword = XSSCheck(currentPassword, 0);
  let password = $('#edit-password').val();
  password = XSSCheck(password, 0);
  let passwordCheck = $('#edit-passwordCheck').val();
  passwordCheck = XSSCheck(passwordCheck, 0);
  let firstPreferredCategoryId = $('#firstPreferredCategoryId').val();
  let secondPreferredCategoryId = $('#secondPreferredCategoryId').val();

  if (firstPreferredCategoryId === secondPreferredCategoryId) {
    alert("1순위 선호 주종과 2순위 선호 주종이 같습니다!");
    return;
  }

  let data = {
    "nickname": nickname,
    "currentPassword": currentPassword,
    "password": password,
    "passwordCheck": passwordCheck,
    "firstPreferredCategoryId": firstPreferredCategoryId,
    "secondPreferredCategoryId": secondPreferredCategoryId,
  }

  $.ajax({
    type: 'PATCH',
    url: `/v1/users/${userId}`,
    contentType: "application/json",
    data: JSON.stringify(data),
    success: function (response) {
      alert(response['msg']);
      window.location.reload();
    },
    error(error, status, request) {
      if (error['responseJSON']['data']) {
        alert(JSON.stringify(error['responseJSON']['data']));
      } else {
        alert(error['responseJSON']['msg']);
      }
    }
  })
}
