package com.example.jujuassembly.domain.product.dto;

import com.example.jujuassembly.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private List<ReviewResponseDto> reviewList;

    public ProductResponseDto(Product product) {
        this.image = product.getImage();
        this.name = product.getName();
        this.description = product.getDescription();
        this.area = product.getArea();
        this.company = product.getCompany();
        this.alcoholDegree = product.getAlcoholDegree();

        this.reviewList = product.getReviewList().stream().map(ReviewResponseDto::new).toList();
    }
}
