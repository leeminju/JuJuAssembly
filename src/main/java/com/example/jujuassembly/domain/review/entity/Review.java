package com.example.jujuassembly.domain.review.entity;

import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.review.dto.ReviewRequestDto;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "reviews")
public class Review extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;// 리뷰 ID

  @Column(nullable = false)
  private String description; //리뷰 내용

  @Column(nullable = false)
  private Double star;// 별점

  @Column
  private String munchies;//먹거리 정보

  @Column(name = "is_verified", nullable = false)
  private Boolean isVerified;// 리뷰 인증 여부

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;// 해당 리뷰가 속한 제품

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;// 해당 리뷰를 작성한 사용자

  @OneToMany(mappedBy = "review", orphanRemoval = true)
  private Set<ReviewImage> reviewImages = new LinkedHashSet<>();

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ReviewLike> reviewLikes = new LinkedHashSet<>();

  // Review 객체를 생성하는 생성자
  public Review(ReviewRequestDto requestDto, Product product, User user) {
    this.description = requestDto.getDescription();
    this.munchies = requestDto.getMunchies();
    this.star = requestDto.getStar();
    this.isVerified = false;
    this.product = product;
    this.user = user;
  }

  // Review 객체를 업데이트하는 메서드
  public void update(ReviewRequestDto requestDto) {
    this.description = requestDto.getDescription();
    this.munchies = requestDto.getMunchies();
    this.star = requestDto.getStar();
    this.isVerified = false;//수정 했다면 인증을 다시 받아야함.
  }

  // 리뷰를 인증 처리하는 메서드
  public void changeVerified() {
    this.isVerified = !this.getIsVerified();
  }
}
