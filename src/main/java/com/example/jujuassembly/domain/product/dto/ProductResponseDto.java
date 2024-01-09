package com.example.jujuassembly.domain.product.dto;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.review.dto.ReviewResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    }
}
