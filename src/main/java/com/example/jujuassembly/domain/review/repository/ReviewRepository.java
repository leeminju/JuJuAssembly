package com.example.jujuassembly.domain.review.repository;

import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {

  Page<Review> findAllByUser(User user, Pageable pageable);

  Page<Review> findAllByProduct(Product product, Pageable pageable);
}
