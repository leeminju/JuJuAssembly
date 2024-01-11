package com.example.jujuassembly.domain.category;

import com.example.jujuassembly.domain.category.dto.CategoryRequestDto;
import com.example.jujuassembly.domain.category.entity.Category;

public interface CategoryTest {

  String ANOTHER_PREDIX = "another-";
  Long TEST_CATEGORY_ID = 1L;
  Long TEST_ANOTHER_CATEGORY_ID = 2L;
  String TEST_CATEGORY_NAME = "name";

  CategoryRequestDto requestDto = CategoryRequestDto.builder().name(TEST_CATEGORY_NAME).build();

  CategoryRequestDto anotherRequestDto = CategoryRequestDto.builder()
      .name(ANOTHER_PREDIX + TEST_CATEGORY_NAME).build();

  Category TEST_CATEGORY = Category.builder()
      .id(TEST_CATEGORY_ID)
      .name(TEST_CATEGORY_NAME)
      .build();

  Category TEST_ANOTHER_CATEGORY = Category.builder()
      .id(TEST_ANOTHER_CATEGORY_ID)
      .name(ANOTHER_PREDIX+TEST_CATEGORY_NAME)
      .build();

}