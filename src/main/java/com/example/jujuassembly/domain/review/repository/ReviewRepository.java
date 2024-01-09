package com.example.jujuassembly.domain.review.repository;

import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {

  List<Review> findAllByWriterOrderByModifiedAtDesc(User user);
}
