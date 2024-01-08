package com.example.jujuassembly.domain.product.service;

import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductResponseDto;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final S3Uploader s3Uploader;
    private final ProductRepository productRepository;

    // 이미지 업로드 + 상품 등록
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto, MultipartFile image, User user) {
        // 관리자 권한 확인
        if (!user.getRole().equals(UserRoleEnum.ADMIN.getAuthority())) {
            throw new IllegalArgumentException("관리자만 상품을 등록할 수 있습니다.");
        }

        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            if (!image.getContentType().startsWith("image")) {
                throw new IllegalArgumentException("이미지 파일 형식이 아닙니다.");
            }
            imageUrl = s3Uploader.upload(image, "images");
        }

        Product product = new Product(requestDto, imageUrl);
        product = productRepository.save(product);
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
        categoryRepository.findById(categoryId).orElseThrow(()
                -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(ProductResponseDto::new);
    }

    // 상품 상세 조회
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(()
                -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        categoryRepository.findById(categoryId).orElseThrow(()
                -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        return new ProductResponseDto(product);
    }



    // 상품 수정
    @Transactional
    public ProductResponseDto updateProduct(Long productId, ProductRequestDto requestDto, User user) {
        Product product = productRepository.findById(productId).orElseThrow(()
                -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        // 관리자 권한 확인
        if (!user.getRole().equals(UserRoleEnum.ADMIN.getAuthority())) {
            throw new IllegalArgumentException("관리자만 상품을 수정할 수 있습니다.");
        }

        product.update(requestDto);
        return new ProductResponseDto(product);

    }

    // 상품 삭제
    public void deleteProduct(Long productId, User user) {
        Product product = productRepository.findById(productId).orElseThrow(()
                -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        // 관리자 권한 확인
        if (!user.getRole().equals(UserRoleEnum.ADMIN.getAuthority())) {
            throw new IllegalArgumentException("관리자만 상품을 삭제할 수 있습니다.");
        }
        
        productRepository.delete(product);
    }
}
