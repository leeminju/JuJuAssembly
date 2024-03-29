package com.example.jujuassembly.domain.category.controller;

import com.example.jujuassembly.domain.category.dto.CategoryRequestDto;
import com.example.jujuassembly.domain.category.dto.CategoryResponseDto;
import com.example.jujuassembly.domain.category.service.CategoryService;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum.Authority;
import com.example.jujuassembly.global.response.ApiResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/categories")
public class CategoryController {


  private final CategoryService categoryService;

  /**
   * 카테고리 목록 조회
   *
   * @return 카테고리 목록과 함께 성공 응답 반환
   */
  @GetMapping
  public ResponseEntity<ApiResponse> getCategories() {
    List<CategoryResponseDto> categoryList = categoryService.getCategories();
    return ResponseEntity.status(HttpStatus.OK)
        .body(new ApiResponse("카테고리 목록 조회 성공", HttpStatus.OK.value(), categoryList));
  }

  /**
   * 카테고리 정보 조회
   *
   * @param categoryId 카테고리 아이디
   * @return
   */
  @GetMapping("/{categoryId}")
  public ResponseEntity<ApiResponse> getCategoryInfo(@PathVariable Long categoryId) {
    CategoryResponseDto categoryinfo = categoryService.getCategoryInfo(categoryId);
    return ResponseEntity.status(HttpStatus.OK)
        .body(new ApiResponse("카테고리 정보 조회 성공", HttpStatus.OK.value(), categoryinfo));
  }

  /**
   * 카테고리 생성
   *
   * @param image      카테고리 이미지 파일
   * @param requestDto 카테고리 요청 DTO
   * @return 생성된 카테고리와 함께 성공 응답 반환
   * @throws IOException
   */
  @Secured(Authority.ADMIN)
  @PostMapping
  public ResponseEntity<ApiResponse> createCategory(
      @RequestParam MultipartFile image,
      @RequestPart("data") CategoryRequestDto requestDto) throws IOException {
    CategoryResponseDto categoryResponseDto = categoryService.createCategory(requestDto, image);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse("카테고리 생성 성공", HttpStatus.CREATED.value(), categoryResponseDto));
  }

  /**
   * 카테고리 수정
   *
   * @param image      카테고리 이미지 파일
   * @param requestDto 카테고리 요청 DTO
   * @param categoryId 수정할 카테고리 ID
   * @return 수정된 카테고리와 함께 성공 응답 반환
   * @throws IOException
   */
  @Secured(Authority.ADMIN)
  @PatchMapping("/{categoryId}")
  public ResponseEntity<ApiResponse> updateCategory(
      @RequestParam MultipartFile image,
      @RequestPart("data") CategoryRequestDto requestDto,
      @PathVariable Long categoryId) throws IOException {
    CategoryResponseDto categoryResponseDto = categoryService.updateCategory(requestDto, categoryId,
        image);
    return ResponseEntity.status(HttpStatus.OK)
        .body(new ApiResponse("카테고리 수정 성공", HttpStatus.OK.value(), categoryResponseDto));
  }

  /**
   * 카테고리 삭제
   *
   * @param categoryId 삭제할 카테고리 ID
   * @return 성공 응답 반환
   */
  @Secured(Authority.ADMIN)
  @DeleteMapping("/{categoryId}")
  public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long categoryId) {
    categoryService.deleteCategory(categoryId);
    return ResponseEntity.status(HttpStatus.OK)
        .body(new ApiResponse("카테고리 삭제 성공", HttpStatus.OK.value()));
  }
}
