package com.example.jujuassembly.domain.report.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.report.dto.ReportRequestDto;
import com.example.jujuassembly.global.entity.Timestamped;
import com.example.jujuassembly.domain.user.entity.User;
import jakarta.persistence.*;
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
@Table(name = "reports")
public class Report extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; //제보상품 ID

  @Column
  private String name;//제보상품 이름

  @Column
  private String image;//제보상품 이미지 주소

  @Column
  @Enumerated
  private StatusEnum status;//제보상품 상태(대기중,채택,비채택)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user; //상품제보한 사람 id

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category; //제보한 상품 카테고리 id

  //제보상품 생성시 ReportRequestDto을 파라미터로 받고 상태는 대기중이 기본값으로 설정하는 메서드
  public Report(ReportRequestDto requestDto) {
    this.name = requestDto.getName();
    this.status = StatusEnum.PROCEEDING;
  }

  //제보상품의 user값 변경(추가)하는 메서드
  public void updateUser(User user) {
    this.user = user;
  }

  //제보상품의 category값 변경(추가)하는 메서드
  public void updateCategory(Category category) {
    this.category = category;
  }

  //제보상품의 이미지 변경(추가)하는 메서드
  public void updateImage(String image) {
    this.image = image;
  }

  //제보상품의 상품명 변경(추가)하는 메서드
  public void updateName(String name) {
    this.name = name;
  }

  //제보상품의 상태를 변경해주는 메서드
  public void updateStatus(StatusEnum status) {
    this.status = status;
  }
}

