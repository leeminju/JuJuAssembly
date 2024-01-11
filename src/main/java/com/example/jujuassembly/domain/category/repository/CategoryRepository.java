package com.example.jujuassembly.domain.category.repository;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.global.exception.ApiException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  default Category getById(Long id) {
    return findById(id).orElseThrow(
        () -> new ApiException("해당 카테고리 상품이 아닙니다.", HttpStatus.NOT_FOUND)
    );
  }
}
