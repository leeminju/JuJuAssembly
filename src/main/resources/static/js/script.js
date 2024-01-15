document.getElementById('productForm').addEventListener('submit', function(event) {
  event.preventDefault();

  const name = document.getElementById('name').value;
  const description = document.getElementById('description').value;
  const price = document.getElementById('price').value;
  const area = document.getElementById('area').value;
  const company = document.getElementById('company').value;
  const alcoholDegree = document.getElementById('alcoholDegree').value;
  const image = document.getElementById('image').files[0];
  debugger
  const categoryId = document.getElementById('categoryId').value;
const data =   {name: name,
      description: description,
      price: price,
      area: area,
      company: company,
      alcoholDegree: alcoholDegree}

  const formData = new FormData();
  formData.append('image', image);
  formData.append("data", new Blob([JSON.stringify(data)], {type: "application/json"}));

 /* const auth = getToken();

  if (auth !== undefined && auth !== '') {
    $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
      jqXHR.setRequestHeader('Authorization', auth);
    });
  } else {
    return;
  }*/

/*  function getToken() {
    let auth = Cookies.get('Authorization');

    if (auth === undefined) {
      return '';
    }

    return auth;
  }*/



  // Ajax 요청 (jQuery 사용)
  $.ajax({
    url: '/v1/categories/'+ categoryId + '/products',
    method: 'POST',
    data: formData,
    enctype: "multipart/form-data",

    contentType: false,
    processData: false,
    success: function(response) {
      console.log('상품이 성공적으로 등록되었습니다.');
    },
    error: function(error) {
      console.error('상품 등록에 실패하였습니다.', error);
    }
  });
});

