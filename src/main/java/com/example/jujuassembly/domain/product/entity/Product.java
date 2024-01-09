package com.example.jujuassembly.domain.product.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.review.entity.Review;
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

}
