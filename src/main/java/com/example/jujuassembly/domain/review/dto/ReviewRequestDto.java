package com.example.jujuassembly.domain.review.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReviewRequestDto {

  @NotBlank
  private String description;
  @JsonInclude(value = Include.ALWAYS)
  private Double star;
  @JsonInclude(value = Include.NON_DEFAULT)
  private String munchies;
}
