package com.example.jujuassembly.domain.review.entity;

import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.review.dto.ReviewRequestDto;
import com.example.jujuassembly.domain.reviewImage.entity.ReviewImage;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.entity.Timestamped;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
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
  private User writer;

  @OneToMany(mappedBy = "review")
  private Set<ReviewImage> reviewImages = new LinkedHashSet<>();

  public Review(ReviewRequestDto requestDto, Product product, User user) {
    this.description = requestDto.getDescription();
    this.munchies = requestDto.getMunchies();
    this.star = requestDto.getStar();
    this.isVerified = false;
    this.product = product;
    this.writer = user;
  }
}
