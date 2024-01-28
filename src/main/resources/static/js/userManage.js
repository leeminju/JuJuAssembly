$(document).ready(function () {
  getAllUsers();
})

function getAllUsers() {
  var sorting = $("#sorting option:selected").val();
  var isAsc = $(':radio[name="isAsc"]:checked').val();

  let dataSource = `/v1/users?sort=${sorting},${isAsc}`;

  $('#userList').empty();
  $('#pagination').pagination({
    dataSource,
    locator: 'data.content',
    alias: {
      pageNumber: 'page',
      pageSize: 'size'
    },
    totalNumberLocator: (response) => {
      return response.data.totalElements;//전체 갯수
    },
    pageSize: 9,
    showPrevious: true,
    showNext: true,
    ajax: {
      error(error, status, request) {
        console.log(error);
      }
    },
    callback: function (response, pagination) {
      $('#userList').empty();
      let users = response;
      for (let i = 0; i < users.length; i++) {
        let user = users[i];
        let userId = user['id'];
        let image = user['image'];
        let loginId = user['loginId']
        let nickname = user['nickname']
        let email = user['email']
        let role = user['role'];

        if (!image) {
          image = "/images/default_user_image.png"
        }
        let html = `<div class="d-flex text-body-secondary pt-3">
        <img style="margin: 7px; width: 40px;height: 40px;object-fit: contain;" src=${image}>
        <p class="pb-3 mb-0 small lh-sm border-bottom"  style="display: flex;flex-direction: row;justify-content: center;">
           <div style="width: 67%">
             <strong class="d-block text-gray-dark">${loginId}(${nickname})</strong>
             email : ${email}<br>
             role : ${role}
           </div>
           <div style="display: flex;flex-direction: row;align-items: center">
              <button style="margin-right: 10px" class="btn btn-light" onclick="location.href='/userReview?userId=${userId}'">리뷰 목록</button>
              <button style="margin-right: 10px" class="btn btn-light" onclick="location.href='/userReport?userId=${userId}'">제보 목록</button>
               <button class="btn btn-light dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
               권한 변경
               </button>
                  <ul class="dropdown-menu">
                     <li onclick="changeRole(${userId}, 'ADMIN')"><a class="dropdown-item" href="#">ADMIN</a></li>
                     <li onclick="changeRole(${userId}, 'USER')"><a class="dropdown-item" href="#">USER</a></li>
                    <li onclick="changeRole(${userId}, 'BANNED')"><a class="dropdown-item" href="#">BANNED</a></li>
                   </ul>
            </div>
  
        </p>
    </div>`;
        $('#userList').append(html);
      }
    }, error(error, status, request) {
      console.log(error);
    }
  });
}

function changeRole(userId, role) {
  $.ajax({
    type: 'PATCH'
    , url: `/v1/users/${userId}/role`
    , contentType: "application/json"
    , data: JSON.stringify({"userRole": role})
    , success: function (response) {
      alert(response['msg']);
      window.location.reload();
    }, error(error, status, request) {
      console.log(error);
    }
  });
}
