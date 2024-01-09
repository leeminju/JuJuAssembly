package com.example.jujuassembly.domain.emailAuth.entity;

import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "email_auth")
public class EmailAuth extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 기존에 입력된 데이터를 받아오는 부분 **/
  @Column(nullable = false)
  private String loginId;

  @Column(nullable = false)
  private String nickname;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private Long firstPreferredCategoryId;

  @Column(nullable = false)
  private Long secondPreferredCategoryId;

  /** 기존에 입력된 데이터를 받아오는 부분 **/
  private String sentCode;

  public EmailAuth(String loginId, String nickname, String email, String password, Long firstPreferredCategoryId, Long secondPreferredCategoryId, String sentCode) {
    this.loginId = loginId;
    this.nickname = nickname;
    this.email = email;
    this.password = password;
    this.firstPreferredCategoryId = firstPreferredCategoryId;
    this.secondPreferredCategoryId = secondPreferredCategoryId;
    this.sentCode = sentCode;
  }
}
