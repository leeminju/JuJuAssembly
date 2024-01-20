package com.example.jujuassembly.domain.product.controller;

import com.example.jujuassembly.domain.product.dto.ProductModifyRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductResponseDto;
import com.example.jujuassembly.domain.product.service.ProductService;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum.Authority;
import com.example.jujuassembly.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/v1")
public class ProductController {

  private final ProductService productService;

  /**
   * 이미지 업로드와 함께 상품을 등록합니다.
   *
   * @param categoryId 상품을 등록할 카테고리의 ID
   * @param image      업로드할 이미지 파일 (선택적)
   * @param requestDto 상품 등록에 필요한 요청 데이터
   * @return 상품 등록 성공 여부를 반환하는 ApiResponse
   * @throws Exception 이미지 업로드 시 발생할 수 있는 예외
   */
  @Secured(Authority.ADMIN)
  @PostMapping("/categories/{categoryId}/products")
  public ResponseEntity<ApiResponse> createProduct(@PathVariable Long categoryId,
      @RequestParam MultipartFile image,
      @RequestPart("data") @Valid ProductRequestDto requestDto) throws Exception {
    productService.createProduct(categoryId, requestDto, image);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse<>("상품 등록에 성공하였습니다.", HttpStatus.CREATED.value()));
  }

  /**
   * 전체 상품 목록을 조회합니다. 페이지네이션과 정렬 기능을 제공합니다.
   *
   * @param pageable 페이지네이션 정보
   * @return 상품 목록과 상태 정보를 담은 ApiResponse
   */
  @GetMapping("/products")
  public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProducts(
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ProductResponseDto> page = productService.getProducts(pageable);
    return ResponseEntity.ok()
        .body(new ApiResponse<>("상품 전체 조회에 성공하였습니다.", HttpStatus.OK.value(), page));
  }

  /**
   * 특정 카테고리에 속하는 상품 목록을 조회합니다. 페이지네이션과 정렬 기능을 제공합니다.
   *
   * @param categoryId 조회할 카테고리의 ID
   * @param pageable   페이지네이션 정보
   * @return 카테고리별 상품 목록과 상태 정보를 담은 ApiResponse
   */
  @GetMapping("/categories/{categoryId}/products")
  public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProductsByCategory(
      @PathVariable Long categoryId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ProductResponseDto> page = productService.getProductsByCategory(categoryId, pageable);
    return ResponseEntity.ok()
        .body(new ApiResponse<>("카테고리별 상품 조회에 성공하였습니다.", HttpStatus.OK.value(), page));
  }

  /**
   * 상품의 상세 정보를 조회합니다.
   *
   * @param productId  조회할 상품의 ID
   * @param categoryId 상품이 속한 카테고리의 ID
   * @return 상품 상세 정보와 상태 정보를 담은 ApiResponse
   */
  @GetMapping("/categories/{categoryId}/products/{productId}")
  public ResponseEntity<ApiResponse> getProduct(@PathVariable Long productId,
      @PathVariable Long categoryId) {
    ProductResponseDto responseDto = productService.getProduct(productId, categoryId);
    return ResponseEntity.ok()
        .body(new ApiResponse<>("상품 상세 조회에 성공하였습니다.", HttpStatus.OK.value(), responseDto));
  }

  /**
   * 키워드를 기반으로 상품을 검색합니다. 페이지네이션과 정렬 기능을 제공합니다.
   *
   * @param keyword  검색할 키워드
   * @param pageable 페이지네이션 정보
   * @return 검색 결과와 상태 정보를 담은 ApiResponse
   */
  @GetMapping("/products/search")
  public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProductsBySearch(
      @RequestParam String keyword,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ProductResponseDto> responseDtoList = productService.getProductsBySearch(keyword,
        pageable);

    return ResponseEntity.ok()
        .body(new ApiResponse<>("상품 검색에 성공하였습니다.", HttpStatus.OK.value(), responseDtoList));
  }

  /**
   * 상품 정보를 수정합니다. 관리자 권한이 필요합니다.
   *
   * @param categoryId 수정할 상품이 속한 카테고리의 ID
   * @param productId  수정할 상품의 ID
   * @param image      수정할 상품의 새 이미지 파일 (선택적)
   * @param requestDto 수정할 상품 정보를 담은 요청 데이터
   * @return 상품 수정 성공 여부를 반환하는 ApiResponse
   * @throws IOException 이미지 업로드 시 발생할 수 있는 예외
   */
  @Secured(Authority.ADMIN)
  @PatchMapping("/categories/{categoryId}/products/{productId}")
  public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long categoryId,
      @PathVariable Long productId,
      @RequestParam(value = "image", required = false) MultipartFile image,
      @RequestPart("data") @Valid ProductModifyRequestDto requestDto) throws IOException {
    productService.updateProduct(categoryId, productId, requestDto, image);
    return ResponseEntity.ok().body(new ApiResponse<>("상품 수정에 성공하였습니다.", HttpStatus.OK.value()));
  }

  /**
   * 상품을 삭제합니다. 관리자 권한이 필요합니다.
   *
   * @param productId 삭제할 상품의 ID
   * @return 상품 삭제 성공 여부를 반환하는 ApiResponse
   */
  @Secured(Authority.ADMIN)
  @DeleteMapping("/categories/{categoryId}/products/{productId}")
  public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId) {
    productService.deleteProduct(productId);
    return ResponseEntity.ok().body(new ApiResponse<>("상품 삭제에 성공하였습니다.", HttpStatus.OK.value()));
  }


}
