package com.example.jujuassembly.domain.product;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductResponseDto;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.product.service.ProductService;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.global.s3.S3Manager;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private S3Manager s3Manager;

    @Mock
    private Pageable pageable;

    @Mock
    private Page<Product> productPage;

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
        productPage = mock(Page.class);

    }

    @Test
    @DisplayName("상품 생성 테스트")
    public void createProductTest() throws Exception {
        // given
        categoryId = 1L;
        productId = 100L;

        // when
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
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

        ProductResponseDto responseDto = productService.createProduct(categoryId, requestDto, image, user);

        // then
        assertNotNull(responseDto);
        assertEquals(imageUrl, responseDto.getImage());
        assertEquals(requestDto.getName(), responseDto.getName());
        assertEquals(requestDto.getDescription(), responseDto.getDescription());
        assertEquals(requestDto.getArea(), responseDto.getArea());
        assertEquals(requestDto.getCompany(), responseDto.getCompany());
        assertEquals(requestDto.getAlcoholDegree(), responseDto.getAlcoholDegree());
    }

//    @Test
//    @DisplayName("전체 상품 조회 + 페이징 테스트")
//    public void getProductsTest() throws Exception {
//        // given
//        categoryId = 1L;
//        productId = 100L;
//
//        List<Product> productList = new ArrayList<>();
//        productList.add(new Product(requestDto));
//        productList.add(new Product());
//
//
//        // when
//        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(productList));
//        when(productPage.getContent()).thenReturn(productList);
//
//        List<ProductResponseDto> responseDto = productService.getProducts(pageable);
//
//        // then
//        assertNotNull(responseDto);
//        assertEquals(productList.size(), responseDto.size());
//    }
//
//    @Test
//    @DisplayName("상세 상품 조회")
//    public void getProductTest() throws Exception {
//        // given
//        categoryId = 1L;
//        productId = 100L;
//
//        List<Product> productList = new ArrayList<>();
//        productList.add(new Product(requestDto));
//        productList.add(new Product());
//
//
//        // when
//        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(productList));
//        when(productPage.getContent()).thenReturn(productList);
//
//        List<ProductResponseDto> responseDto = productService.getProducts(pageable);
//
//        // then
//        assertNotNull(responseDto);
//        assertEquals(productList.size(), responseDto.size());
//    }
//
//    @Test
//    @DisplayName("상품 검색 + 페이징 테스트")
//    public void searchProductsTest() throws Exception {
//        // given
//        categoryId = 1L;
//        productId = 100L;
//
//        List<Product> productList = new ArrayList<>();
//        productList.add(new Product(requestDto));
//        productList.add(new Product());
//
//
//        // when
//        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(productList));
//        when(productPage.getContent()).thenReturn(productList);
//
//        List<ProductResponseDto> responseDto = productService.getProducts(pageable);
//
//        // then
//        assertNotNull(responseDto);
//        assertEquals(productList.size(), responseDto.size());
//    }
//
@Test
@DisplayName("상품 수정 테스트")
public void updateProductTest() throws Exception {
    // given
    Long categoryId = 1L;
    Long productId = 100L;
    String imageUrl = "https://test.com/updated-image.jpg";

    // 테스트용 수정 DTO 초기화
    ProductRequestDto testUpdateDto = ProductRequestDto.builder()
            .name("Updated Product")
            .description("Updated Description")
            .area("Updated Area")
            .company("Updated Company")
            .alcoholDegree(5.5)
            .build();

    Product mockProduct = mock(Product.class);
    when(mockProduct.getName()).thenReturn("Updated Product");
    when(mockProduct.getDescription()).thenReturn("Updated Description");
    when(mockProduct.getArea()).thenReturn("Updated Area");
    when(mockProduct.getCompany()).thenReturn("Updated Company");
    when(mockProduct.getAlcoholDegree()).thenReturn(5.5);
    when(mockProduct.getImage()).thenReturn(imageUrl);

    // Repository 모의 설정
    when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
    when(categoryRepository.existsById(categoryId)).thenReturn(true);

    // MultipartFile 객체 초기화 및 모의 설정
    MultipartFile image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(false);
    when(s3Manager.upload(eq(image), eq("products"), eq(productId))).thenReturn(imageUrl);

    // ProductService의 updateProduct 호출
    ProductResponseDto updatedProduct = productService.updateProduct(categoryId, productId, testUpdateDto, image, user);

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
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // when
        productService.deleteProduct(productId, user);


        // then
        // 상품이 삭제되었는지 확인
        verify(productRepository, times(1)).delete(product);

        // 이미지 파일 삭제가 호출되었는지 확인
        verify(s3Manager, times(1)).deleteAllImageFiles(productId.toString(), "products");
    }


}
