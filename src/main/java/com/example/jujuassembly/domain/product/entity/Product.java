package com.example.jujuassembly.domain.product.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.like.entity.Like;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
  private Long id;

  @Column
  private String image;

  @Column
  private String name;

  @Column
  private String description;

  @Column
  private String area;

  @Column
  private String company;

  @Column
  private Double alcoholDegree;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Review> reviews = new LinkedHashSet<>();
  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  private Set<Like> likes = new LinkedHashSet<>();

  public Product(ProductRequestDto requestDto, Category category) {
    this.name = requestDto.getName();
    this.description = requestDto.getDescription();
    this.area = requestDto.getArea();
    this.company = requestDto.getCompany();
    this.alcoholDegree = requestDto.getAlcoholDegree();
    this.category = category;

  }

  public void update(ProductRequestDto requestDto) {
    this.name = requestDto.getName();
    this.description = requestDto.getDescription();
    this.alcoholDegree = requestDto.getAlcoholDegree();
    this.company = requestDto.getCompany();
  }

  public void setImage(String url) {
    this.image = url;
  }

  // 리뷰 개수 반환
  public int getReviewCount() {
    return reviews.size();
  }

  // 찜(좋아요) 횟수 반환
  public int getLikesCount() {
    return likes.size();
  }
}
