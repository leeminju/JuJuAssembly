package com.example.jujuassembly.domain.user.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.like.entity.Like;
import com.example.jujuassembly.domain.user.dto.UserModifyRequestDto;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  @Column
  @Setter
  private String image;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "first_preferred_category_id")
  private Category firstPreferredCategory;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "second_preferred_category_id")
  private Category secondPreferredCategory;

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


}
