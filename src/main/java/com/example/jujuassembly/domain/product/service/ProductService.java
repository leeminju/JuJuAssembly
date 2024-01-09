package com.example.jujuassembly.domain.product.service;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductResponseDto;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.s3.S3Manager;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final S3Manager s3Manager;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // 이미지 업로드 + 상품 등록
    @Transactional
    public ProductResponseDto createProduct(Long categoryId, ProductRequestDto requestDto, MultipartFile image, User user) throws Exception {
        Category category = categoryRepository.findById(categoryId).orElseThrow(()
                -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));


        Product product = new Product(requestDto, category);
        product = productRepository.save(product);


        if (image != null && !image.isEmpty()) {
            if (!image.getContentType().startsWith("image")) {
                throw new IllegalArgumentException("이미지 파일 형식이 아닙니다.");
            }
            String imageUrl = s3Manager.upload(image, "products", product.getId());
            product.setImage(imageUrl);
        }

        return new ProductResponseDto(product);
    }

    // 상품 전체 조회 + 페이징 정렬(상품 등록순)
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        // "Page" 인터페이스가 제공하는 'map' 메서드 활용
        return products.getContent().stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    // 카테고리별 상품 조회 + 페이징 정렬(상품 등록순)
    public List<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable) {

        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다.");
        }

        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.getContent().stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    // 상품 상세 조회
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Long productId, Long categoryId) {
        Product product = productRepository.findById(productId).orElseThrow(()
                -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다.");
        }

        return new ProductResponseDto(product);
    }



    // 상품 수정
    @Transactional
    public ProductResponseDto updateProduct(Long categoryId, Long productId, ProductRequestDto requestDto, MultipartFile image, User user) throws IOException {
        Product product = productRepository.findById(productId).orElseThrow(()
                -> new IllegalArgumentException("상품이 존재하지 않습니다."));


        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다.");
        }

        //기존의 파일 모두 삭제
        s3Manager.deleteAllImageFiles(productId.toString(), "products");

        //새로 업로드
        if (image!=null && !image.isEmpty()){
            String url = s3Manager.upload(image,"products",productId);
            product.setImage(url);
        }

        product.update(requestDto);
        return new ProductResponseDto(product);

    }

    // 상품 삭제
    public void deleteProduct(Long productId, User user) {
        Product product = productRepository.findById(productId).orElseThrow(()
                -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        //기존의 파일 모두 삭제
        s3Manager.deleteAllImageFiles(productId.toString(), "products");

        productRepository.delete(product);
    }
}
