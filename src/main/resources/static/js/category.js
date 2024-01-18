var create_imageFile = null;
var update_imageFile = null;
$(document).ready(function () {
  getCategory();
  $("#create_category_image").on("change", function (event) {
    create_imageFile = event.target.files[0];
  });
  $("#update_category_image").on("change", function (event) {
    update_imageFile = event.target.files[0];
  });
})

function win_reload() {
  window.location.reload();
}

function getCategory() {
  $.ajax({
    type: "GET",
    url: `/v1/categories`,
    success: function (response) {
      let categories = response['data'];
      for (let i = 0; i < categories.length; i++) {
        let category = categories[i];
        let id = category['id'];
        let name = category['name'];
        let image = category['image'];

        if(!image){
          image = "/images/default_category_image.png";
        }

        let html = `<div style="flex-direction: row;display: flex;margin: 10px" className="d-flex text-body-secondary pt-3">
        <img style="margin-right: 20px ;width: 100px;height: 120px;object-fit: contain" src="${image}">

        <div style="width: 75%">${id} : ${name}</div>
        <div><button onclick="updateId('${id}','${name}')" data-bs-toggle="modal"
            data-bs-target="#updateCategory"
            class="btn btn-light" style="margin-left:10px;float: right">수정</button>
          <button onclick="removeCategory(${id})" class="btn btn-light" style="float: right">삭제</button></div>
        </div>`;

        $('#categoryList').append(html);

      }
    }
  });
}

function updateId(id, name) {
  $('#update_category_name').val(name);
  $('#update_btn').val(id);

}

function updateCategory() {
  if (!update_imageFile) {
    alert("이미지를 선택해주세요");
  }

  let id = $('#update_btn').val();

  let name = $('#update_category_name').val();
  let data = {
    "name": name
  }

  const formData = new FormData();
  formData.append('image', update_imageFile);
  formData.append("data",
      new Blob([JSON.stringify(data)], {type: "application/json"}));

  $.ajax({
    type: "PATCH",
    url: `/v1/categories/${id}`,
    data: formData,
    enctype: "multipart/form-data",
    contentType: false,
    processData: false,
    success: function (response) {
      alert(response['msg']);
      window.location.reload();
    }, error: function (error) {
      alert(error['responseJSON']['msg'])
    }
  });
}

function removeCategory(id) {
  $.ajax({
    type: "DELETE",
    url: `/v1/categories/${id}`,
    success: function (response) {
      alert(response['msg']);
      window.location.reload();
    }, error: function (error) {
      alert(error['response']['msg']);
    }
  });
}

function createCategory() {

  if (!create_imageFile) {
    alert("이미지를 선택해주세요");
  }
  let name = $('#create_category_name').val();
  let data = {
    "name": name
  }

  const formData = new FormData();
  formData.append('image', create_imageFile);
  formData.append("data",
      new Blob([JSON.stringify(data)], {type: "application/json"}));

  $.ajax({
    type: "POST",
    url: `/v1/categories`,
    data: formData,
    enctype: "multipart/form-data",
    contentType: false,
    processData: false,
    success: function (response) {
      alert(response['msg']);
      window.location.reload();
    }, error: function (error) {
      alert(error['responseJSON']['msg']);
    }
  });
}

