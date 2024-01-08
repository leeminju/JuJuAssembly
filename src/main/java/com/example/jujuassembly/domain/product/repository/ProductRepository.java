package com.example.jujuassembly.domain.product.repository;

import com.example.jujuassembly.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 카테고리 ID를 기준으로 상품 조회
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
}
