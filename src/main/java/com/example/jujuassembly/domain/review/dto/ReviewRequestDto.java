package com.example.jujuassembly.domain.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
  @Size(min = 1, max = 255, message = "리뷰 설명은 1자 ~ 255자 이내로 작성해 주세요")
  private String description;

  @Digits(integer = 1, fraction = 1, message = "소수점 1자리 이하로 입력해주세요.")
  @DecimalMin(value = "1.0", message = "1.0 ~ 5.0 범위의 값을 입력해주세요.")
  @DecimalMax(value = "5.0", message = "1.0 ~ 5.0 범위의 값을 입력해주세요.")
  private Double star;

  @NotBlank(message = "추천 안주를 입력해주세요!")
  @Size(min = 1, max = 30, message = "추천 안주는 1자 ~ 30자 이내로 작성해 주세요")
  private String munchies;
}
