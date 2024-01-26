package com.example.jujuassembly.domain.product.service;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.product.dto.ProductModifyRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductResponseDto;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.s3.S3Manager;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final S3Manager s3Manager;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  // 이미지 업로드 + 상품 등록
  @Transactional
  public ProductResponseDto createProduct(Long categoryId, ProductRequestDto requestDto,
      MultipartFile image) throws Exception {
    Category category = categoryRepository.findCategoryByIdOrElseThrow(categoryId);

    Product product = new Product(requestDto, category);
    product = productRepository.save(product);

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.", HttpStatus.BAD_REQUEST);
      }
      String imageUrl = s3Manager.upload(image, S3Manager.PRODUCT_DIRECTORY_NAME, product.getId());
      product.setImage(imageUrl);
    }

    return new ProductResponseDto(product);
  }

  // 상품 전체 조회 + 페이징 정렬(상품 등록순)
  @Transactional(readOnly = true)
  public Page<ProductResponseDto> getProducts(Pageable pageable) {
    Page<Product> products = productRepository.findAll(pageable);
    // "Page" 인터페이스가 제공하는 'map' 메서드 활용
    return products.map(ProductResponseDto::new);
  }

  // 카테고리별 상품 조회 + 페이징 정렬(상품 등록순)
  public Page<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable) {

    if (!categoryRepository.existsById(categoryId)) {
      throw new ApiException("해당 카테고리가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
    }

    Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
    return products.map(ProductResponseDto::new);
  }

  // 상품 상세 조회
  @Transactional(readOnly = true)
  public ProductResponseDto getProduct(Long productId, Long categoryId) {
    Product product = productRepository.findProductByIdOrElseThrow(productId);

    if (!categoryRepository.existsById(categoryId)) {
      throw new ApiException("해당 카테고리가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
    }

    return new ProductResponseDto(product);
  }

  // 상품 검색
  public Page<ProductResponseDto> getProductsBySearch(String keyword, Pageable pageable) {
    if (!StringUtils.hasText(keyword)) {
      throw new ApiException("검색어를 입력해주세요.", HttpStatus.BAD_REQUEST);
    }

    Page<Product> products = productRepository.findByKeyword(keyword, pageable);
    return products.map(ProductResponseDto::new);
  }


  // 상품 수정
  @Transactional
  public ProductResponseDto updateProduct(Long categoryId, Long productId,
      ProductModifyRequestDto requestDto, MultipartFile image) throws IOException {
    Product product = productRepository.findProductByIdOrElseThrow(productId);

    if (!categoryRepository.existsById(categoryId)) {
      throw new ApiException("해당 카테고리가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
    }
    Category category = categoryRepository.findCategoryByIdOrElseThrow(
        requestDto.getModifiedCategoryId());

    //기존의 파일 모두 삭제
    s3Manager.deleteAllImageFiles(productId.toString(), S3Manager.PRODUCT_DIRECTORY_NAME);

    //새로 업로드
    if (image != null && !image.isEmpty()) {
      String url = s3Manager.upload(image, S3Manager.PRODUCT_DIRECTORY_NAME, productId);
      product.setImage(url);
    }

    product.update(requestDto, category);
    return new ProductResponseDto(product);

  }

  // 상품 삭제
  public void deleteProduct(Long productId) {
    Product product = productRepository.findProductByIdOrElseThrow(productId);

    //기존의 파일 모두 삭제
    s3Manager.deleteAllImageFiles(productId.toString(), S3Manager.PRODUCT_DIRECTORY_NAME);

    productRepository.delete(product);
  }

}
