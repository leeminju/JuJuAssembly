$(document).ready(function () {
  // productI와 categoryId를 URL에서 받아옴
  const urlParams = new URLSearchParams(window.location.search);
  const productId = urlParams.get('productId');
  const categoryId = urlParams.get('categoryId');

  const formData = new FormData();
  formData.append('image', image);
  formData.append("data",
      new Blob([JSON.stringify(data)], {type: "application/json"}));
  // Ajax요청
  $.ajax({
    method: 'GET',
    url: `/categories/${categoryId}/products/${productId}`,
    success: function (response) {
      // Display product details in the container
      displayProductDetails(response);
    },
    error: function () {
      // Handle error if the request fails
      alert('Failed to fetch product details.');
    }
  });
// Ajax 요청 (jQuery 사용)
  $.ajax({
    url: '/v1/categories/' + categoryId + '/products',
    method: 'POST',
    data: formData,
    enctype: "multipart/form-data",

    contentType: false,
    processData: false,
    success: function (response) {
      console.log('상품이 성공적으로 등록되었습니다.');
    },
    error: function (error) {
      console.error('상품 등록에 실패하였습니다.', error);
    }
  });
});
