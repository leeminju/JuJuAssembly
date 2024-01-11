package com.example.jujuassembly.domain.category.service;

import com.example.jujuassembly.domain.category.dto.CategoryRequestDto;
import com.example.jujuassembly.domain.category.dto.CategoryResponseDto;
import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.s3.S3Manager;
import java.io.IOException;
import java.util.List;
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

  public List<CategoryResponseDto> getCategories() {
    List<Category> categoryList = categoryRepository.findAll();
    return categoryList.stream().map(CategoryResponseDto::new).toList();
  }

  @Transactional
  public CategoryResponseDto createCategory(CategoryRequestDto requestDto, MultipartFile image)
      throws IOException {
    Category category = new Category(requestDto);
    categoryRepository.save(category);

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.",HttpStatus.BAD_REQUEST);
      }
      String imageUrl = s3Manager.upload(image, "categories", category.getId());
      category.updateImage(imageUrl);
    }

    return new CategoryResponseDto(category);
  }


  @Transactional
  public CategoryResponseDto updateCategory(CategoryRequestDto requestDto, Long categoryId,
      MultipartFile image) throws IOException {
    Category category = categoryRepository.findById(categoryId).orElseThrow(()->new ApiException("해당 카테고리가 존재하지 않습니다.",HttpStatus.BAD_REQUEST));
    category.updateName(requestDto);

    s3Manager.deleteAllImageFiles(categoryId.toString(), "categories");

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.",HttpStatus.BAD_REQUEST);
      }
      String imageUrl = s3Manager.upload(image, "categories", category.getId());
      category.updateImage(imageUrl);
    }

    return new CategoryResponseDto(category);
  }

  @Transactional
  public void deleteCategory(Long categoryId) {
    Category category = categoryRepository.findById(categoryId).orElseThrow(()->new ApiException("해당 카테고리가 존재하지 않습니다.",
        HttpStatus.BAD_REQUEST));
    categoryRepository.delete(category);
    s3Manager.deleteAllImageFiles(categoryId.toString(), "categories");
  }
}
