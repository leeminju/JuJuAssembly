let userId = -1;

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

$(document).ready(async function () {
  getCategory();

  try {
    const response = await $.ajax({
      type: 'GET',
      url: `/v1/users/myprofile`
    });

    userInfo = response['data'];
    userId = userInfo['id'];
    $('#loginId').text(userInfo['loginId']);
    $('#nickname').text(userInfo['nickname']);
    $('#email').text(userInfo['email']);

    let firstId = userInfo['firstPreferredCategoryId'];
    let secondId = userInfo['secondPreferredCategoryId'];

    if (firstId != null) {
      let firstName = await getCategoryName(firstId);
      $('#firstPreferredCategory').text(firstName);
    }
    if (secondId != null) {
      let secondName = await getCategoryName(secondId);
      $('#secondPreferredCategory').text(secondName);
    }

    let image = userInfo['image'];
    if (!image) {
      image = "/images/default_user_image.png"
    }
    $('#image').attr('src', image);
  } catch (error) {
    alert(error['responseJSON']['msg']);
  }

  $('#image').click(function () {
    $('#imageInput').click();
  });

  // 파일 입력 창의 내용이 변경되면 호출되는 함수
  $('#imageInput').change(function (event) {
    // 선택한 파일 정보 가져오기
    const selectedFile = event.target.files[0];

    if (selectedFile) {
      // 선택한 파일을 미리보기에 표시
      console.log(userId);
      $('#image').attr('src', URL.createObjectURL(selectedFile));
      const formData = new FormData();
      formData.append('image', selectedFile);

      $.ajax({
        type: 'POST',
        url: `/v1/users/${userId}`,
        data: formData,
        enctype: "multipart/form-data",
        contentType: false,
        processData: false,
        success: function (response) {
          alert(response['msg']);
          window.location.reload();
        },
        error(error, status, request) {
          alert(error['responseJSON']['msg'])
        }
      })
      ;
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

function updateProfile() {
  let nickname = $('#edit-nickname').val();
  let currentPassword = $('#current_password').val();
  let password = $('#edit-password').val();
  let passwordCheck = $('#edit-passwordCheck').val();
  let firstPreferredCategoryId = $('#firstPreferredCategoryId').val();
  let secondPreferredCategoryId = $('#secondPreferredCategoryId').val();
  let email = $('#edit-email').val();

  console.log(firstPreferredCategoryId + " " + secondPreferredCategoryId);

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
    "email": email
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
      console.log(error);
      alert(error['responseJSON']['msg'])
    }
  })
}
