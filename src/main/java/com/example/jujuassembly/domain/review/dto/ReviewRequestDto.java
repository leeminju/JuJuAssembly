package com.example.jujuassembly.domain.review.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewRequestDto {

  @NotBlank(message = "리뷰 설명을 입력해주세요!")
  private String description;

  @JsonInclude(value = Include.ALWAYS)
  @Digits(integer = 1, fraction = 1, message = "소수점 1자리 이하로 입력해주세요.")
  @DecimalMin(value = "1.0", message = "1.0 ~ 5.0 범위의 값을 입력해주세요.")
  @DecimalMax(value = "5.0", message = "1.0 ~ 5.0 범위의 값을 입력해주세요.")
  private Double star;

  @JsonInclude(value = Include.NON_DEFAULT)
  private String munchies;
}
