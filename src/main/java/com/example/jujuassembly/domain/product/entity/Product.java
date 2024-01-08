package com.example.jujuassembly.domain.product.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.product.dto.ProductRequestDto;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
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

    @OneToMany(mappedBy = "product")
    private Set<Review> reviews = new LinkedHashSet<>();

    public Product(ProductRequestDto requestDto, String imageUrl) {
        this.image = imageUrl;
        this.name = requestDto.getName();
        this.description = requestDto.getDescription();
        this.area = requestDto.getArea();
        this.company = requestDto.getCompany();
        this.alcoholDegree = requestDto.getAlcoholDegree();

    }

    public void update(ProductRequestDto requestDto) {
        this.image = requestDto.getImage();
        this.name = requestDto.getName();
        this.description = requestDto.getDescription();
        this.alcoholDegree = requestDto.getAlcoholDegree();
        this.company = requestDto.getCompany();
    }
}
