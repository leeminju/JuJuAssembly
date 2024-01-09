package com.example.jujuassembly.domain.user.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "first_preferred_category_id")
  private Category firstPreferredCategory;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "second_preferred_category_id")
  private Category secondPreferredCategory;


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

}
