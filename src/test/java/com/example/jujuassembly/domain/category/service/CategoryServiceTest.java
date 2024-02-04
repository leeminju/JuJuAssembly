package com.example.jujuassembly.domain.category.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.dto.CategoryRequestDto;
import com.example.jujuassembly.domain.category.dto.CategoryResponseDto;
import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.report.dto.ReportRequestDto;
import com.example.jujuassembly.domain.report.entity.Report;
import com.example.jujuassembly.domain.report.entity.StatusEnum;
import com.example.jujuassembly.domain.report.repository.ReportRepository;
import com.example.jujuassembly.domain.review.repository.ReviewRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.UserTestUtil;
import com.example.jujuassembly.global.s3.S3Manager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest implements UserTestUtil {

  @InjectMocks
  CategoryService categoryService;
  @Mock
  CategoryRepository categoryRepository;
  @Mock
  S3Manager s3Manager;
  @Mock
  UserRepository userRepository;
  @Mock
  ReportRepository reportRepository;
  @Mock
  ProductRepository productRepository;
  @Mock
  ReviewRepository reviewRepository;

  @Test
  void getCategoriesTest() {
    //given
    // 가짜 Category 목록 생성
    CategoryRequestDto requestDto2 = CategoryRequestDto.builder().name("카테고리2").build();
    Category category1 = new Category(requestDto);
    Category category2 = new Category(requestDto2);
    List<Category> fakeCategoryList = Arrays.asList(category1, category2);

    // CategoryRepository.findAll() 메서드가 호출될 때 가짜 Category 목록 반환
    when(categoryRepository.findAll()).thenReturn(fakeCategoryList);

    //when
    // 테스트 대상 메서드 호출
    List<CategoryResponseDto> result = categoryService.getCategories();

    //then
    // 예상 결과 생성
    List<CategoryResponseDto> expected = Arrays.asList(
        new CategoryResponseDto(category1),
        new CategoryResponseDto(category2)
    );

    // 결과 검증
    assertEquals(expected.get(0).getName(), result.get(0).getName());
    assertEquals(expected.get(1).getName(), result.get(1).getName());

    // CategoryRepository.findAll() 메서드가 한 번 호출되었는지 확인
    verify(categoryRepository, times(1)).findAll();


  }

  @Test
  void createCategoryTest() throws IOException {
    //given
    Category category = new Category(requestDto);
    category.builder().id(TEST_CATEGORY_ID).build();
    //이미지
    MultipartFile image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(false);
    when(image.getContentType()).thenReturn("image/jpeg");
    String mockImageUrl = "https://example.com/image.jpg";

    when(s3Manager.upload(any(), eq("categories"), eq(category.getId()))).thenReturn(mockImageUrl);

    CategoryResponseDto result = categoryService.createCategory(requestDto, image);

    // Then
    verify(categoryRepository, times(1)).save(any());

    // 이미지가 업로드되었는지 확인
    verify(s3Manager, times(1)).upload(eq(image), eq("categories"), eq(category.getId()));

    // CategoryResponseDto가 올바르게 생성되었는지 확인
    assertEquals(category.getId(), result.getId());
    assertEquals(category.getName(), result.getName());
    assertEquals(mockImageUrl, result.getImage());

  }

  @Test
  void updateCategoryTest() throws IOException {
    // Given
    CategoryRequestDto categoryRequestDto = CategoryRequestDto.builder().name("UpdatedCategory")
        .build();
    MultipartFile image = mock(MultipartFile.class);
    when(image.isEmpty()).thenReturn(false);
    when(image.getContentType()).thenReturn("image/jpeg");
    CategoryRequestDto requestDto1 = CategoryRequestDto.builder().name("ExistCategory").build();

    Category existingCategory = new Category(requestDto1);
    existingCategory.builder().image("https://exapmple.com/image.jpg").build();
    when(categoryRepository.findCategoryByIdOrElseThrow(TEST_CATEGORY_ID)).thenReturn(
        existingCategory);

    //when
    String mockImageUrl = "https://modified.com/image.jpg";
    when(s3Manager.upload(any(), eq("categories"), eq(existingCategory.getId()))).thenReturn(
        mockImageUrl);
    CategoryResponseDto result = categoryService.updateCategory(categoryRequestDto,
        TEST_CATEGORY_ID, image);

    // Then
    // CategoryRepository.findCategoryByIdOrElseThrow() 메소드가 호출되었는지 확인
    verify(categoryRepository, times(1)).findCategoryByIdOrElseThrow(TEST_CATEGORY_ID);

    // S3Manager.deleteAllImageFiles() 메소드가 호출되었는지 확인
    verify(s3Manager, times(1)).deleteAllImageFiles(TEST_CATEGORY_ID.toString(), "categories");

    // 이미지가 업로드되었는지 확인
    verify(s3Manager, times(1)).upload(eq(image), eq("categories"), eq(existingCategory.getId()));

    // CategoryResponseDto가 올바르게 생성되었는지 확인
    assertEquals(existingCategory.getId(), result.getId());
    assertEquals(categoryRequestDto.getName(), result.getName());
    // 이미지 업데이트 여부 확인 (이 부분은 이미지 업데이트 로직에 따라 달라질 수 있음)
    assertEquals(existingCategory.getImage(), result.getImage());

  }

  @Test
  void deleteCategoryTest() {
    // Given
    Long categoryId = 1L;
    CategoryRequestDto categoryRequestDto = CategoryRequestDto.builder().name("ExistingCategory")
        .build();
    Category existingCategory = new Category(categoryRequestDto);
    when(categoryRepository.findCategoryByIdOrElseThrow(categoryId)).thenReturn(existingCategory);

    List<User> userList = new ArrayList<>();
    userList.add(TEST_USER);
    when(userRepository.findAllByFirstPreferredCategory_Id(categoryId)).thenReturn(userList);
    when(userRepository.findAllBySecondPreferredCategory_Id(categoryId)).thenReturn(userList);

    Report report = Report.builder().id(1L).name("TEST_REPORT_NAME").image("TEST_IMAGE_URL").status(
        StatusEnum.UN_ADOPTED).user(TEST_USER).category(TEST_CATEGORY).build();
    List<Report> reportList = new ArrayList<>();
    reportList.add(report);
    when(reportRepository.findAllByCategory_Id(categoryId)).thenReturn(reportList);

    //카테고리초기화
    ReportRequestDto requestDto = ReportRequestDto.builder().name("제보상품이름").build();
    Category category = new Category(CategoryRequestDto.builder().name("카테고리1").build());
    ReflectionTestUtils.setField(category, Category.class, "id", 1L, Long.class);
    //유저초기화
    User user = new User("loginId", "nickname", "email", "password", category, category);
    ReflectionTestUtils.setField(user, User.class, "id", 1L, Long.class);
    //리포트 초기화
    Report report2 = new Report(requestDto);
    ReflectionTestUtils.setField(report2, Report.class, "id", 1L, Long.class);

    CategoryRequestDto categoryRequestDto2 = CategoryRequestDto.builder().name("ExistingCategory")
        .build();
    Category existingCategory2 = new Category(categoryRequestDto2);
    when(categoryRepository.findCategoryByIdOrElseThrow(category.getId())).thenReturn(existingCategory2);
    when(reportRepository.findReportByIdOrElseThrow(report2.getId())).thenReturn(report2);

    Product product = Product.builder().id(1L).image("TEST_IMAGE_URL").name("TEST_PRODUCT_NAME")
        .description("TEST_PRODUCT_DESCRIPTION").area("TEST_PRODUCT_AREA")
        .company("TEST_PRODUCT_COMPANY").alcoholDegree(1d).category(TEST_CATEGORY).build();
    List<Product> productList = new ArrayList<>();
    productList.add(product);
    when(productRepository.findAllByCategory_Id(categoryId)).thenReturn(productList);
    categoryId = 1L;
    Long productId = 100L;

    ProductRequestDto requestDto5 = ProductRequestDto.builder()
        .name("테스트 상품")
        .description("테스트 설명")
        .area("테스트 지역")
        .company("테스트 회사")
        .alcoholDegree(5.0)
        .build();

    Product product2 = new Product(requestDto5, category);
    when(productRepository.findProductByIdOrElseThrow(productId)).thenReturn(product2);

    when(reviewRepository.findAllByProduct_Id(productId)).thenReturn(null);



    // When
    categoryService.deleteCategory(categoryId);

    // Then
    // CategoryRepository.findCategoryByIdOrElseThrow() 메소드가 호출되었는지 확인
    verify(categoryRepository, times(1)).findCategoryByIdOrElseThrow(categoryId);

    // CategoryRepository.delete() 메소드가 호출되었는지 확인
    verify(categoryRepository, times(1)).delete(existingCategory2);

    // S3Manager.deleteAllImageFiles() 메소드가 호출되었는지 확인
    verify(s3Manager, times(1)).deleteAllImageFiles(categoryId.toString(), "categories");
  }
}