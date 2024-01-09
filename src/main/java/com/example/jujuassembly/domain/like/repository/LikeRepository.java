package com.example.jujuassembly.domain.like.repository;

import com.example.jujuassembly.domain.like.entity.Like;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like,Long> {

  List<Like> findAllByUserId(Long userId);

  Like findByProductId(Long productId);
}
