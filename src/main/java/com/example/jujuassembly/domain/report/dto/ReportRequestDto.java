package com.example.jujuassembly.domain.report.dto;

import com.example.jujuassembly.domain.category.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportRequestDto {

  @NotBlank(message = "제보할 상품명을 입력하세요")
  @Size(max = 20,message = "상품명 최대 길이는 20자입니다.")
  private String name;

}
