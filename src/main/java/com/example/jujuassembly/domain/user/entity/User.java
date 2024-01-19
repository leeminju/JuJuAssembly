package com.example.jujuassembly.domain.user.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.like.entity.Like;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.user.dto.UserModifyRequestDto;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
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
@Table(name = "users")
public class User extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; //user Id

  @Column(name = "login_id", nullable = false)
  private String loginId; //user의 로그인아이디

  @Column(name = "nickname", nullable = false)
  private String nickname; //user의 닉네임

  @Column(name = "email", nullable = false)
  private String email; //user의 이메일

  @Column(name = "password", nullable = false)
  private String password; // user의 비밀번호

  @Column(name = "is_archived")
  private Boolean isArchived; //user의 상태

  @Column(nullable = false, name = "role")
  @Enumerated(value = EnumType.STRING)
  private UserRoleEnum role; //user 역할

  @Column(name = "image")
  private String image; //유저의 프로필 사진

  @Column(name = "kakaoId")
  private Long kakaoId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "first_preferred_category_id")
  private Category firstPreferredCategory; //가장 선호하는 술종

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "second_preferred_category_id")
  private Category secondPreferredCategory; //두번째 선호하는 술종

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Review> reviews = new LinkedHashSet<>(); //상품 리뷰 리스트

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ReviewLike> reviewLikes = new LinkedHashSet<>(); //리뷰 좋아요 리스트

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private Set<Like> likes = new LinkedHashSet<>(); //좋아요 한 상품 리스트


  //유저 생성자
  public User(String loginId, String nickname, String email, String password,
      Category firstPreferredCategory, Category secondPreferredCategory) {
    this.loginId = loginId;
    this.nickname = nickname;
    this.email = email;
    this.password = password;
    this.isArchived = false;
    this.role = UserRoleEnum.USER;
    this.firstPreferredCategory = firstPreferredCategory;
    this.secondPreferredCategory = secondPreferredCategory;
  }

  public User(String loginId, String nickname, String email, String password, Long kakaoId,
      String url, Category firstPreferredCategory, Category secondPreferredCategory) {
    this.loginId = loginId;
    this.nickname = nickname;
    this.email = email;
    this.password = password;
    this.isArchived = false;
    this.role = UserRoleEnum.USER;
    this.firstPreferredCategory = firstPreferredCategory;
    this.secondPreferredCategory = secondPreferredCategory;
    this.kakaoId = kakaoId;
    this.image = url;
  }

  //유저 정보 수정
  public void updateUser(UserModifyRequestDto modifyRequestDto, String encodePassword,
      Category category1,
      Category category2) {
    this.nickname = modifyRequestDto.getNickname();
    this.email = modifyRequestDto.getEmail();
    this.password = encodePassword;
    this.firstPreferredCategory = category1;
    this.secondPreferredCategory = category2;
  }

  //유저 상태 설정
  public void setIsArchived(boolean b) {
    this.isArchived = b;
  }

  //유저 권한 수정
  public void changeRole(UserRoleEnum userRoleEnum) {
    this.role = userRoleEnum;
  }

  //유저 프로필 이미지 설정
  public void updateUserImage(String url) {
    this.image = url;
  }

  public User kakaoIdUpdate(Long kakaoId) {
    this.kakaoId = kakaoId;
    return this;
  }

  public boolean hasSameId(Long id) {
    return this.id.equals(id);
  }


}
