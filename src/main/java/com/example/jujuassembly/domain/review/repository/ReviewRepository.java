package com.example.jujuassembly.domain.review.repository;

import com.example.jujuassembly.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {

}
