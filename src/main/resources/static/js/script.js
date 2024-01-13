document.getElementById('productForm').addEventListener('submit', function(event) {
  event.preventDefault();

  const categoryId = document.getElementById('categoryId').value;
  const productName = document.getElementById('productName').value;
  const price = document.getElementById('price').value;
  const image = document.getElementById('image').files[0];

  const formData = new FormData();
  formData.append('image', image);
  formData.append('data', JSON.stringify({
    productName: productName,
    price: price
    // 기타 필드 추가
  }));

  // Ajax 요청 예시 (jQuery 사용)
  $.ajax({
    url: `/v1/categories/${categoryId}/products`,
    method: 'POST',
    data: formData,
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
