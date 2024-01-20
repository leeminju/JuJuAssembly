package com.example.jujuassembly.domain.like.repository;

import com.example.jujuassembly.domain.like.entity.Like;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

  List<Like> findAllByUserId(Long userId);

  Page<Like> findAllByUser(User user, Pageable pageable);

  boolean existsByProductAndUser(Product product, User user);

  Optional<Like> findByProductAndUser(Product product, User user);
}
