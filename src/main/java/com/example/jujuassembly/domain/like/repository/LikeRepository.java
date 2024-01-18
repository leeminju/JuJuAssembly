package com.example.jujuassembly.domain.like.repository;

import com.example.jujuassembly.domain.like.entity.Like;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like,Long> {

  List<Like> findAllByUserId(Long userId);

  Like findByProductId(Long productId);

  boolean existsByProductAndUser(Product product, User user);
}
