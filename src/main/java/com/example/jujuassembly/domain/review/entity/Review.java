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
  private Long id;

  @Column
  private String description;

  @Column
  private Double star;

  @Column
  private String munchies;

  @Column(name = "is_verified")
  private Boolean isVerified;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ReviewImage> reviewImages = new LinkedHashSet<>();

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ReviewLike> reviewLikes = new LinkedHashSet<>();

  public Review(ReviewRequestDto requestDto, Product product, User user) {
    this.description = requestDto.getDescription();
    this.munchies = requestDto.getMunchies();
    this.star = requestDto.getStar();
    this.isVerified = false;
    this.product = product;
    this.user = user;
  }

  public void update(ReviewRequestDto requestDto) {
    this.description = requestDto.getDescription();
    this.munchies = requestDto.getMunchies();
    this.star = requestDto.getStar();
    this.isVerified = false;//수정 했다면 인증을 다시 받아야함.
  }

  public void verify() {
    this.isVerified = true;
  }
}
