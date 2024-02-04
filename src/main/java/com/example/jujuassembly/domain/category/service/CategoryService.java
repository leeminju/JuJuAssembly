package com.example.jujuassembly.domain.category.service;

import com.example.jujuassembly.domain.category.dto.CategoryRequestDto;
import com.example.jujuassembly.domain.category.dto.CategoryResponseDto;
import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.product.service.ProductService;
import com.example.jujuassembly.domain.report.repository.ReportRepository;
import com.example.jujuassembly.domain.report.service.ReportService;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.s3.S3Manager;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final S3Manager s3Manager;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final ProductService productService;
  private final ReportRepository reportRepository;
  private final ReportService reportService;

  public List<CategoryResponseDto> getCategories() {
    List<Category> categoryList = categoryRepository.findAll();
    return categoryList.stream().map(CategoryResponseDto::new).toList();
  }

  public CategoryResponseDto getCategoryInfo(Long categoryId) {
    Category category = categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    return new CategoryResponseDto(category);
  }

  @Transactional
  public CategoryResponseDto createCategory(CategoryRequestDto requestDto, MultipartFile image)
      throws IOException {
    Category category = new Category(requestDto);
    categoryRepository.save(category);

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.", HttpStatus.BAD_REQUEST);
      }
      String imageUrl = s3Manager.upload(image, S3Manager.CATEGORY_DIRECTORY_NAME,
          category.getId());
      category.updateImage(imageUrl);
    }

    return new CategoryResponseDto(category);
  }


  @Transactional
  public CategoryResponseDto updateCategory(CategoryRequestDto requestDto, Long categoryId,
      MultipartFile image) throws IOException {
    Category category = categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    category.updateName(requestDto);

    s3Manager.deleteAllImageFiles(categoryId.toString(), S3Manager.CATEGORY_DIRECTORY_NAME);

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.", HttpStatus.BAD_REQUEST);
      }
      String imageUrl = s3Manager.upload(image, S3Manager.CATEGORY_DIRECTORY_NAME,
          category.getId());
      category.updateImage(imageUrl);
    }

    return new CategoryResponseDto(category);
  }

  @Transactional
  public void deleteCategory(Long categoryId) {

    List<User> usersWithFirstPreferredCategory = userRepository.findAllByFirstPreferredCategory_Id(
        categoryId);
    if (usersWithFirstPreferredCategory != null) {
      usersWithFirstPreferredCategory.stream().forEach(User::deleteFirstPreferredCategoryCategory);
    }
    List<User> usersWithSecondPreferredCategory = userRepository.findAllBySecondPreferredCategory_Id(
        categoryId);
    if (usersWithSecondPreferredCategory != null) {
      usersWithSecondPreferredCategory.stream()
          .forEach(User::deleteSecondPreferredCategoryCategory);
    }

    Optional.ofNullable(reportRepository.findAllByCategory_Id(categoryId))
        .orElse(Collections.emptyList())
        .stream()
        .forEach(report -> {
          reportService.deleteReport(categoryId, report.getId());
        });

    Optional.ofNullable(productRepository.findAllByCategory_Id(categoryId))
        .orElse(Collections.emptyList())
        .stream().forEach(product -> {
          productService.deleteProduct(product.getId());
        });

    Category category = categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    categoryRepository.delete(category);
    s3Manager.deleteAllImageFiles(categoryId.toString(), S3Manager.CATEGORY_DIRECTORY_NAME);
  }


}
