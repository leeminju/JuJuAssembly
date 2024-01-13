// 페이지 로드 시 실행될 함수
window.onload = function() {
  // 상품 정보를 가져와서 표시하는 함수 호출 (예시 데이터 사용)
  displayProduct(getExampleProduct());
};

// 상품 정보를 가져와서 화면에 표시하는 함수
function displayProduct(product) {
  const productDetailsDiv = document.getElementById('productDetails');
  productDetailsDiv.innerHTML = '';

  const productDiv = document.createElement('div');
  productDiv.innerHTML = `
        <img src="${product.image}" alt="Product Image">
        <h2>${product.name}</h2>
        <p>Description: ${product.description}</p>
        <p>Area: ${product.area}</p>
        <p>Company: ${product.company}</p>
        <p>Alcohol Degree: ${product.alcoholDegree}</p>
        <p>Category: ${product.categoryName}</p>
        <p>Review Count: ${product.reviewCount}</p>
        <p>Average Rating: ${product.averageRating}</p>
        <p>Likes Count: ${product.likesCount}</p>
    `;

  // 리뷰 목록 표시
  if (product.reviewList && product.reviewList.length > 0) {
    const reviewListDiv = document.createElement('div');
    reviewListDiv.innerHTML = '<h3>Reviews</h3>';

    product.reviewList.forEach(review => {
      const reviewDiv = document.createElement('div');
      reviewDiv.innerHTML = `
                <p>User: ${review.userName}</p>
                <p>Star Rating: ${review.star}</p>
                <p>Comment: ${review.comment}</p>
                <hr>
            `;
      reviewListDiv.appendChild(reviewDiv);
    });

    productDiv.appendChild(reviewListDiv);
  }

  productDetailsDiv.appendChild(productDiv);
}

// 예시 상품 데이터
function getExampleProduct() {
  return {
    image: 'example.jpg',
    name: 'Example Product',
    description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.',
    area: 'Example Area',
    company: 'Example Company',
    alcoholDegree: 10.5,
    categoryName: 'Example Category',
    reviewCount: 3,
    averageRating: 4.7,
    likesCount: 20,
    reviewList: [
      { userName: 'User1', star: 5, comment: 'Great product!' },
      { userName: 'User2', star: 4, comment: 'Good value for money.' },
      { userName: 'User3', star: 4.5, comment: 'Nice taste.' }
    ]
  };
}
