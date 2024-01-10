package com.example.jujuassembly.domain.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRequestDto {
    @NotBlank(message = "상품명을 입력하세요.")
    private String name;

    @Size(min = 10, max = 200, message = "상품 설명을 입력하세요.")
    private String description;

    @NotBlank(message = "지역을 입력하세요.")
    private String area;

    @NotBlank(message = "제조회사를 입력해주세요.")
    private String company;

    @NotNull(message = "상품의 도수를 입력해주세요.")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double alcoholDegree;
}
