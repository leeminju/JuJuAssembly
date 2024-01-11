package com.example.jujuassembly.domain.category.entity;

import com.example.jujuassembly.domain.category.dto.CategoryRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "categories")
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 카테고리 ID

  @Column(nullable = false)
  private String name; //카테고리 이름

  @Column
  private String image; //카테고리 이미지 주소

  //CategoryRequestDto를 매개변수로 받아서 Category를 생성하는 생성자.
  public Category(CategoryRequestDto requestDto) {
    this.name = requestDto.getName();
  }

  //Category 이름을 업데이트(변경) 메서드
  public void updateName(CategoryRequestDto requestDto) {
    this.name = requestDto.getName();
  }

  //Category 사진을 업데이트(변경) 메서드
  public void updateImage(String image) {
    this.image = image;
  }
}
