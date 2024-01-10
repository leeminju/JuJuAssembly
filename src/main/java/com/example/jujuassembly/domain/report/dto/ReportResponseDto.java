package com.example.jujuassembly.domain.report.dto;

import com.example.jujuassembly.domain.category.dto.CategoryResponseDto;
import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.report.entity.Report;
import com.example.jujuassembly.domain.report.entity.StatusEnum;
import com.example.jujuassembly.domain.user.dto.UserResponseDto;
import com.example.jujuassembly.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReportResponseDto {
  private Long id;

  private String name;

  private String image;

  private StatusEnum status;

  private UserResponseDto user;

  private CategoryResponseDto category;

  public ReportResponseDto(Report report){
    this.id = report.getId();
    this.name = report.getName();
    this.image = report.getImage();
    this.status = report.getStatus();
    this.user = new UserResponseDto(report.getUser());
    this.category = new CategoryResponseDto(report.getCategory());
  }
}
