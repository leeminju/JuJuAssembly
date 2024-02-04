package com.example.jujuassembly.domain.category.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.jujuassembly.domain.category.dto.CategoryRequestDto;
import com.example.jujuassembly.domain.category.dto.CategoryResponseDto;
import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.product.service.ProductService;
import com.example.jujuassembly.domain.report.entity.Report;
import com.example.jujuassembly.domain.report.repository.ReportRepository;
import com.example.jujuassembly.domain.report.service.ReportService;
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

  @InjectMocks
  ReportService reportService;
  @InjectMocks
  ProductService productService;

  @Test
  void deleteCategory() {
    // Given
    Long categoryId = 1L;
    CategoryRequestDto categoryRequestDto = CategoryRequestDto.builder().name("ExistingCategory")
        .build();
    Category existingCategory = new Category(categoryRequestDto);
    when(categoryRepository.findCategoryByIdOrElseThrow(categoryId)).thenReturn(existingCategory);

    when(userRepository.findAllByFirstPreferredCategory_Id(categoryId)).thenReturn(null);
    when(userRepository.findAllBySecondPreferredCategory_Id(categoryId)).thenReturn(null);

    Report TEST_REPORT = Report.builder().id(1L).user(TEST_USER).category(TEST_CATEGORY).build();
    List<Report> reportList = new ArrayList<>();
    reportList.add(TEST_REPORT);
    when(reportRepository.findAllByCategory_Id(categoryId)).thenReturn(reportList);
    doNothing().when(reportService).deleteReport(any(Long.class), any(Long.class));

    Product TEST_PRODUCT = Product.builder().category(TEST_CATEGORY).reviews(null).likes(null).build();
    List<Product> productList = new ArrayList<>();
    productList.add(TEST_PRODUCT);
    when(productRepository.findAllByCategory_Id(categoryId)).thenReturn(productList);
    doNothing().when(productService).deleteProduct(any(Long.class));

    // When
    categoryService.deleteCategory(categoryId);

    // Then
    // CategoryRepository.findCategoryByIdOrElseThrow() 메소드가 호출되었는지 확인
    verify(categoryRepository, times(1)).findCategoryByIdOrElseThrow(categoryId);

    // CategoryRepository.delete() 메소드가 호출되었는지 확인
    verify(categoryRepository, times(1)).delete(existingCategory);

    // S3Manager.deleteAllImageFiles() 메소드가 호출되었는지 확인
    verify(s3Manager, times(1)).deleteAllImageFiles(categoryId.toString(), "categories");
  }
}