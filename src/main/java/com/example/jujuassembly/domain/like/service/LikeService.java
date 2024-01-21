package com.example.jujuassembly.domain.like.service;

import com.example.jujuassembly.domain.like.dto.LikeResponseDto;
import com.example.jujuassembly.domain.like.entity.Like;
import com.example.jujuassembly.domain.like.repository.LikeRepository;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.product.repository.ProductRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.repository.UserRepository;
import com.example.jujuassembly.global.exception.ApiException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LikeService {

  private final ProductRepository productRepository;
  private final LikeRepository likeRepository;
  private final UserRepository userRepository;

  //좋아요
  public void addLike(Long categoryId, Long productId, User user) {
    //상품, 카테고리 존재여부 확인
    Product product = productRepository.getById(productId);

    if (!product.getCategory().getId().equals(categoryId)) {
      throw new ApiException("해당 상품이 해당 카테고리에 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
    }
    //이미 좋아요 한건지 확인
    List<Like> myLikeList = likeRepository.findAllByUserId(user.getId());
    if (myLikeList.stream().anyMatch(like -> like.getProduct().getId().equals(productId))) {
      throw new ApiException("이미 좋아요를 한 상품입니다.", HttpStatus.BAD_REQUEST);
    }

    Like like = new Like(product, user);
    likeRepository.save(like);
  }

  //본인 좋아요 목록 조회
  public Page<LikeResponseDto> viewLikeProducts(Long userId, User loginUser, Pageable pageable) {
    User user = userRepository.getById(userId);

    if (!user.getId().equals(loginUser.getId())) {
      throw new ApiException("본인의 좋아요 목록만 조회 가능힙니다.", HttpStatus.BAD_REQUEST);
    }
    Page<Like> likeList = likeRepository.findAllByUser(user, pageable);

    return likeList.map(LikeResponseDto::new);
  }

  //좋아요 취소
  public void cancelLike(Long productId, User user) {
    Product product = productRepository.getById(productId);
    Like like = likeRepository.findByProductAndUser(product, user).orElseThrow
        (() -> new ApiException("좋아요한 기록이 없습니다.", HttpStatus.NOT_FOUND)
        );

    if (!like.getUser().getId().equals(user.getId())) {
      throw new ApiException("본인만 좋아요 취소가 가능힙니다.", HttpStatus.BAD_REQUEST);
    }

    likeRepository.delete(like);
  }

  public Boolean getLike(Long productId, User user) {
    Product product = productRepository.getById(productId);
    return likeRepository.existsByProductAndUser(product, user);
  }
}
