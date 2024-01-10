package com.example.jujuassembly.domain.product.dto;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.review.dto.ReviewResponseDto;
import com.example.jujuassembly.domain.review.entity.Review;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductResponseDto {
    private String image;
    private String name;
    private String description;
    private String area;
    private String company;
    private Double alcoholDegree;

    private Long categoryId;
    private String categoryName;

    private List<ReviewResponseDto> reviewList;
    private int reviewCount; // 리뷰 수
    private double averageRating; // 별점 평균
    private int likesCount; // 찜 횟수 추가

    public ProductResponseDto(Product product) {
        this.image = product.getImage();
        this.name = product.getName();
        this.description = product.getDescription();
        this.area = product.getArea();
        this.company = product.getCompany();
        this.alcoholDegree = product.getAlcoholDegree();

        Category category = product.getCategory();
        if (category != null) {
            this.categoryId = category.getId();
            this.categoryName = category.getName();
        }

        this.reviewList = product.getReviews().stream().map(ReviewResponseDto::new).toList();
        this.reviewCount = product.getReviewCount();
        this.averageRating = calculateAverageRating(product.getReviews());
        this.likesCount = product.getLikesCount();
    }



    // 평균 별점 계산 메서드
    private double calculateAverageRating(Set<Review> reviews) {
        if (reviews.isEmpty()) {
            return 0.0;
        } else {
            return reviews.stream().mapToDouble(Review::getStar).average().orElse(0.0);
        }
    }
}
