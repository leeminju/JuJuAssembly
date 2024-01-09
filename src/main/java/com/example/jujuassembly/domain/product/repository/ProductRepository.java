package com.example.jujuassembly.domain.product.repository;

import com.example.jujuassembly.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
