package com.example.jujuassembly.domain.user.entity;

public enum UserRoleEnum {

  //유저 권한
  USER(Authority.USER),
  ADMIN(Authority.ADMIN),
  BANNED(Authority.BANNED);

  private final String authority; //유저 권한

  //유저 권한 생성자
  UserRoleEnum(String authority) {
    this.authority = authority;
  }

  //유저 권한 조회
  public String getAuthority() {
    return this.authority;
  }

  //유저 권한
  public static class Authority {

    public static final String USER = "ROLE_USER";
    public static final String ADMIN = "ROLE_ADMIN";
    private static final String BANNED = "ROLE_BANNED";
  }
}
