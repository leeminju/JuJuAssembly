package com.example.jujuassembly.domain.product.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.like.entity.Like;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
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
    @OneToMany (mappedBy = "product", cascade = CascadeType.ALL)
    private List<Like> likes = new ArrayList<>();

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
