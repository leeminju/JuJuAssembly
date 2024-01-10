package com.example.jujuassembly.domain.user.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.review.entity.Review;
import com.example.jujuassembly.domain.reviewLike.entity.ReviewLike;
import com.example.jujuassembly.domain.like.entity.Like;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "login_id")
  private String loginId;

  @Column(name = "nickname")
  private String nickname;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "is_archived")
  private Boolean isArchived;

  @Column(nullable = false, name = "role")
  @Enumerated(value = EnumType.STRING)
  private UserRoleEnum role;

  @Column
  private String image;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "first_preferred_category_id")
  private Category firstPreferredCategory;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "second_preferred_category_id")
  private Category secondPreferredCategory;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Review> reviews = new LinkedHashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ReviewLike> reviewLikes = new LinkedHashSet<>();

  @OneToMany (mappedBy = "user", cascade = CascadeType.ALL)
  private List<Like> likes = new ArrayList<>();


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

  public void updateUser(UserModifyRequestDto modifyRequestDto) {
    this.nickname = modifyRequestDto.getNickname();
    this.email = modifyRequestDto.getEmail();
    this.password = modifyRequestDto.getPassword();
    this.firstPreferredCategory = modifyRequestDto.getFirstPreferredCategoryId();
    this.secondPreferredCategory = modifyRequestDto.getSecondPreferredCategoryId();
  }

  public void setIsArchived(boolean b) {
    this.isArchived = b;
  }
  public void changeRole(UserRoleEnum userRoleEnum){
    this.role = userRoleEnum;
  }

  public void updateUserImage(String url){
    this.image = url;
  }


}
