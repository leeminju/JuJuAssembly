package com.example.jujuassembly.domain.product.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class ProductRequestDto {
    private String image;
    private String name;
    private String description;
    private String area;
    private String company;
    private Double alcoholDegree;
}
