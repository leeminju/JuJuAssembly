package com.example.jujuassembly.domain.category.repository;

import com.example.jujuassembly.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
