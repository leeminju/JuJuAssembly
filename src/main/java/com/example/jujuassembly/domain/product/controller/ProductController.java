package com.example.jujuassembly.domain.product.controller;

import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductResponseDto;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.service.ProductService;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/categories/{categoryId}/products")
public class ProductController {

    private final ProductService productService;

    // 이미지 업로드 + 상품 등록
    @PostMapping("/categories/{categoryId}/products")
    public ResponseEntity<ApiResponse> createProduct(@RequestParam(value = "image", required = false) MultipartFile image,
                                                     @RequestPart("data") @Valid ProductRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        productService.createProduct(requestDto, image, userDetails.getUser());
        return ResponseEntity.ok(new ApiResponse<>("상품 등록에 성공하였습니다.", HttpStatus.CREATED.value()));
    }

    // 전체 상품 목록 조회 + 페이징 정렬
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProducts(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponseDto> page = productService.getProducts(pageable);
        return ResponseEntity.ok(new ApiResponse<>("상품 전체 조회에 성공하였습니다.", HttpStatus.OK.value(), page));
    }

    // 카테고리별 상품 목록 조회 + 페이징 정렬
    @GetMapping("/categories/{categoryId}/products")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProductsByCategory(@PathVariable Long categoryId, @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponseDto> page = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(new ApiResponse<>("카테고리별 상품 조회에 성공하였습니다.", HttpStatus.OK.value(), page));
    }

    // 상품 상세 조회
    @GetMapping("/categories/{categoryId}/products/{productId}")
    public ResponseEntity<ApiResponse> getProduct(@PathVariable Long productId) {
        ProductResponseDto responseDto = productService.getProduct(productId);
        return ResponseEntity.ok(new ApiResponse<>("상품 상세 조회에 성공하였습니다.", HttpStatus.OK.value(), responseDto));
    }

    // 상품 수정
    @PatchMapping("/categories/{categoryId}/products/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long productId, @Valid ProductRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        productService.updateProduct(productId, requestDto, userDetails.getUser());
        return ResponseEntity.ok(new ApiResponse<>("상품 수정에 성공하였습니다.", HttpStatus.OK.value()));
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        productService.deleteProduct(productId, userDetails.getUser());
        return ResponseEntity.ok(new ApiResponse<>("상품 삭제에 성공하였습니다.", HttpStatus.OK.value()));
    }


}
