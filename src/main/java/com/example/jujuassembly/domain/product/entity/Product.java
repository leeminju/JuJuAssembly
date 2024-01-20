package com.example.jujuassembly.domain.product.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.like.entity.Like;
import com.example.jujuassembly.domain.product.dto.ProductModifyRequestDto;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.review.entity.Review;
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
@Table(name = "products")
public class Product extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 제품의 고유 식별자

  @Column
  private String image; // 제품 이미지 URL 또는 경로

  @Column(nullable = false)
  private String name; // 제품명

  @Column(nullable = false)
  private String description; // 제품 설명

  @Column(nullable = false)
  private String area; // 제품 출처

  @Column(nullable = false)
  private String company; // 제품을 생산한 회사

  @Column(nullable = false)
  private Double alcoholDegree; // 제품의 알코올 도수

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category; // 제품이 속한 카테고리

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Review> reviews = new LinkedHashSet<>(); // 제품과 연관된 리뷰 집합

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  private Set<Like> likes = new LinkedHashSet<>(); // 제품과 연관된 좋아요 집합

  // ProductRequestDto와 카테고리에서 Product를 생성하는 생성자
  public Product(ProductRequestDto requestDto, Category category) {
    this.name = requestDto.getName();
    this.description = requestDto.getDescription();
    this.area = requestDto.getArea();
    this.company = requestDto.getCompany();
    this.alcoholDegree = requestDto.getAlcoholDegree();
    this.category = category;
  }

  // 제품 이미지 URL 설정 메서드
  public void setImage(String url) {
    this.image = url;
  }

  // 리뷰 개수 반환 메서드
  public int getReviewCount() {
    return reviews.size();
  }

  // 좋아요 개수 반환 메서드
  public int getLikesCount() {
    return likes.size();
  }


  public void update(ProductModifyRequestDto requestDto, Category category) {
    this.name = requestDto.getName();
    this.description = requestDto.getDescription();
    this.alcoholDegree = requestDto.getAlcoholDegree();
    this.company = requestDto.getCompany();
    this.category = category;

  // 평점 평균 반환 메서드,
  public Double getReviewAverage() {
    return reviews.stream()
        .mapToDouble(Review::getStar)
        .average()
        .orElse(0.0);
  }
}
