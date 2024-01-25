package com.example.jujuassembly.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.product.dto.ProductModifyRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductResponseDto;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.global.s3.S3Manager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

  @Mock
  CategoryRepository categoryRepository;

  @Mock
  ProductRepository productRepository;

  @Mock
  S3Manager s3Manager;

  @Mock
  Pageable pageable;


  @InjectMocks
  private ProductService productService;

  private Long categoryId;
  private Long productId;
  private Category category;
  private ProductRequestDto requestDto;
  private MultipartFile image;
  private User user;
  private Product product;

  @BeforeEach
  void setUp() {
    // Category 초기화
    category = Category.builder()
        .name("TestCategory")
        .build();

    // Product 초기화
    product = Product.builder()
        .name("Original Product")
        .description("Original Description")
        .area("Original Area")
        .company("Original Company")
        .alcoholDegree(4.0)
        .build();

    // User 초기화
    user = User.builder()
        .loginId("testUser")
        .nickname("testNickname")
        .email("test@gmail.com")
        .password("Password123!")
        .role(UserRoleEnum.ADMIN)
        // @Secured -> 시큐리티 권한 검증?
        .firstPreferredCategory(category)
        .secondPreferredCategory(category)
        .build();

    // ProductRequestDto 초기화
    requestDto = ProductRequestDto.builder()
        .name("테스트 상품")
        .description("테스트 설명")
        .area("테스트 지역")
        .company("테스트 회사")
        .alcoholDegree(5.0)
        .build();

    // Pageable 및 Page<Product> 객체 초기화
    pageable = mock(Pageable.class);
    // productPage = mock(Page.class);

  }

  @Test
  @DisplayName("상품 생성 테스트")
  public void createProductTest() throws Exception {
    // given
    categoryId = 1L;
    productId = 100L;

    // when
    when(categoryRepository.findCategoryByIdOrElseThrow(categoryId)).thenReturn(category);
    when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
      Product savedProduct = invocation.getArgument(0);
      ReflectionTestUtils.setField(savedProduct, "id", productId); // Reflection을 사용하여 ID 설정
      return savedProduct;
    });

    // MultipartFile 객체 초기화
    image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(false);
    when(image.getContentType()).thenReturn("image/jpeg");

    String imageUrl = "https://test.com/image.jpg";
    when(s3Manager.upload(eq(image), eq("products"), eq(productId))).thenReturn(imageUrl);

    ProductResponseDto responseDto = productService.createProduct(categoryId, requestDto, image
    );

    // then
    assertNotNull(responseDto);
    assertEquals(imageUrl, responseDto.getImage());
    assertEquals(requestDto.getName(), responseDto.getName());
    assertEquals(requestDto.getDescription(), responseDto.getDescription());
    assertEquals(requestDto.getArea(), responseDto.getArea());
    assertEquals(requestDto.getCompany(), responseDto.getCompany());
    assertEquals(requestDto.getAlcoholDegree(), responseDto.getAlcoholDegree());
  }

  @Test
  @DisplayName("전체 상품 조회 + 페이징 테스트")
  public void getProductsTest() throws Exception {
    // given
    pageable = PageRequest.of(0, 10); // 예시 페이지 요청

    // Product 리스트 생성 및 초기화
    List<Product> productList = new ArrayList<>();
    productList.add(new Product(requestDto, category));
    productList.add(new Product(requestDto, category));

    Page<Product> productPage = new PageImpl<>(productList, pageable, productList.size());

    // when
    when(productRepository.findAll(pageable)).thenReturn(productPage);

    // 서비스 메소드 호출
    Page<ProductResponseDto> responseDto = productService.getProducts(pageable);

    // then
    assertNotNull(responseDto);
    assertEquals(productList.size(), responseDto.getContent().size());
  }

  @Test
  @DisplayName("상세 상품 조회")
  public void getProductTest() throws Exception {
    // given
    productId = 100L;
    categoryId = 1L;

    int expectedReviewCount = 10;
    double expectedAverageRating = 4.5;
    int expectedLikesCount = 5;

    // Product 객체에 리뷰 수, 평균 별점, 찜 수 설정
    Product mockProduct = mock(Product.class);
    when(mockProduct.getReviewCount()).thenReturn(expectedReviewCount);
//        when(mockProduct.getaverageRating()).thenReturn(expectedAverageRating);
    when(mockProduct.getLikesCount()).thenReturn(expectedLikesCount);
    // 기타 필요한 Product 메소드 반환 값 설정
    when(mockProduct.getName()).thenReturn("Original Product");
    when(mockProduct.getDescription()).thenReturn("Original Description");
    // ...

    // Repository 모의 설정
    when(productRepository.findProductByIdOrElseThrow(productId)).thenReturn(mockProduct);
    when(categoryRepository.existsById(categoryId)).thenReturn(true);

    // when
    ProductResponseDto responseDto = productService.getProduct(productId, categoryId);

    // then
    assertNotNull(responseDto);
    assertEquals("Original Product", responseDto.getName());
    assertEquals("Original Description", responseDto.getDescription());
    assertEquals(expectedReviewCount, responseDto.getReviewCount());
//        assertEquals(expectedAverageRating, responseDto.getAverageRating(), 0.01);
    assertEquals(expectedLikesCount, responseDto.getLikesCount());
  }

  @Test
  @DisplayName("카테고리별 조회 + 페이징 테스트")
  public void getProductsByCategoryTest() throws Exception {
    // given
    categoryId = 1L;
    pageable = PageRequest.of(0, 10);

    List<Product> productList = new ArrayList<>();
    productList.add(new Product(requestDto, category));
    productList.add(new Product(requestDto, category));

    Page<Product> productPage = new PageImpl<>(productList, pageable, productList.size());

    // Repository 모의 설정
    when(categoryRepository.existsById(categoryId)).thenReturn(true);
    when(productRepository.findByCategoryId(categoryId, pageable)).thenReturn(productPage);

    // when
    Page<ProductResponseDto> responseDtoList = productService.getProductsByCategory(categoryId,
        pageable);

    // then
    assertNotNull(responseDtoList);
    assertEquals(productList.size(), responseDtoList.getContent().size());
  }

  @Test
  @DisplayName("상품 검색 + 페이징 테스트")
  public void searchProductsTest() throws Exception {
    // given
    String keyword = "test";
    pageable = PageRequest.of(0, 10); // 예시 페이지 요청

    List<Product> productList = new ArrayList<>();
    productList.add(new Product(requestDto, category));
    productList.add(new Product(requestDto, category));

    Page<Product> productPage = new PageImpl<>(productList, pageable, productList.size());

    // when
    when(productRepository.findByKeyword(keyword, pageable)).thenReturn(productPage);

    // 서비스 메소드 호출
    Page<ProductResponseDto> responseDtoList = productService.getProductsBySearch(keyword,
        pageable);

    // then
    assertNotNull(responseDtoList);
    assertEquals(productList.size(), responseDtoList.getContent().size());
  }


  @Test
  @DisplayName("상품 수정 테스트")
  public void updateProductTest() throws Exception {
    // given
    categoryId = 1L;
    Long modifiedCategoryId = 2L;
    Category modifideCategory = Category.builder().id(modifiedCategoryId).name("양주").build();
    productId = 100L;
    String imageUrl = "https://test.com/updated-image.jpg";

    // 테스트용 수정 DTO 초기화
    ProductModifyRequestDto testUpdateDto = ProductModifyRequestDto.builder()
        .name("Updated Product")
        .description("Updated Description")
        .area("Updated Area")
        .company("Updated Company")
        .alcoholDegree(5.5)
        .modifiedCategoryId(2L)
        .build();

    Product mockProduct = mock(Product.class);
    when(mockProduct.getName()).thenReturn("Updated Product");
    when(mockProduct.getDescription()).thenReturn("Updated Description");
    when(mockProduct.getArea()).thenReturn("Updated Area");
    when(mockProduct.getCompany()).thenReturn("Updated Company");
    when(mockProduct.getAlcoholDegree()).thenReturn(5.5);
    when(mockProduct.getImage()).thenReturn(imageUrl);
    when(mockProduct.getCategory()).thenReturn(modifideCategory);

    // Repository 모의 설정
    when(productRepository.findProductByIdOrElseThrow(productId)).thenReturn(mockProduct);
    when(categoryRepository.existsById(categoryId)).thenReturn(true);

    // MultipartFile 객체 초기화 및 모의 설정
    MultipartFile image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(false);
    when(s3Manager.upload(eq(image), eq("products"), eq(productId))).thenReturn(imageUrl);

    // ProductService의 updateProduct 호출
    ProductResponseDto updatedProduct = productService.updateProduct(categoryId, productId,
        testUpdateDto, image);

    // then
    assertNotNull(updatedProduct);
    verify(s3Manager).deleteAllImageFiles(productId.toString(), "products"); // 파일 삭제 검증
    verify(s3Manager).upload(eq(image), eq("products"), eq(productId)); // 이미지 업로드 검증
    assertEquals(imageUrl, updatedProduct.getImage()); // 이미지 URL 검증
    assertEquals(testUpdateDto.getName(), updatedProduct.getName()); // 기타 필드 검증
    assertEquals(testUpdateDto.getDescription(), updatedProduct.getDescription());
    assertEquals(testUpdateDto.getArea(), updatedProduct.getArea());
    assertEquals(testUpdateDto.getCompany(), updatedProduct.getCompany());
    assertEquals(testUpdateDto.getAlcoholDegree(), updatedProduct.getAlcoholDegree());
  }

  @Test
  @DisplayName("상품 삭제 테스트")
  public void deleteProductTest() throws Exception {
    // given
    categoryId = 1L;
    productId = 100L;

    Product product = new Product(requestDto, category);
    when(productRepository.findProductByIdOrElseThrow(productId)).thenReturn(product);

    // when
    productService.deleteProduct(productId);

    // then
    // 상품이 삭제되었는지 확인
    verify(productRepository, times(1)).delete(product);

    // 이미지 파일 삭제가 호출되었는지 확인
    verify(s3Manager, times(1)).deleteAllImageFiles(productId.toString(), "products");
  }

}
