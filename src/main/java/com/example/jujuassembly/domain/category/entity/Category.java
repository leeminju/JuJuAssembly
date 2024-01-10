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
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "categories")
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    private String image;

    public Category(CategoryRequestDto requestDto){
        this.name = requestDto.getName();
    }
    public void update(CategoryRequestDto requestDto){
        this.name = requestDto.getName();
    }
    public void updateImage(String image){
        this.image = image;
    }
}
