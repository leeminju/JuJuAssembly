package com.example.jujuassembly.domain.product.repository;

import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.global.exception.ApiException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  default Product findProductByIdOrElseThrow(Long id) {
    return findById(id).orElseThrow(
        () -> new ApiException("해당 상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    );
  }

  // 카테고리 ID를 기준으로 상품 조회.
  Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

  @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
  Page<Product> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

  List<Product> findAllByCategory_Id(Long categoryId);

  //상품 찜 수 증가
  @Modifying
  @Query("UPDATE Product p SET p.likesCount = p.likesCount+1 WHERE p.id = :productId")
  void increaseLikesCount(Long productId);

  //상품 찜 수 감소
  @Modifying
  @Query("UPDATE Product p SET p.likesCount = p.likesCount-1 WHERE p.id = :productId")
  void decreaseLikesCount(Long productId);

  //상품 내 리뷰 수 증가
  @Modifying
  @Query("UPDATE Product p SET p.reviewCount = p.reviewCount+1 WHERE p.id = :productId")
  void increaseReviewCount(Long productId);

  //상품 내 리뷰 수 감소
  @Modifying
  @Query("UPDATE Product p SET p.reviewCount = p.reviewCount-1 WHERE p.id = :productId")
  void decreaseReviewCount(Long productId);
}
