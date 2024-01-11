package com.example.jujuassembly.domain.user.entity;

public enum UserRoleEnum {

  USER(Authority.USER),
  ADMIN(Authority.ADMIN),
  BANNED(Authority.BANNED);

  private final String authority;

  UserRoleEnum(Stroing authority) {
    this.authority = authority;
  }

  public String getAuthority() {
    return this.authority;
  }

  public static class Authority {

    public static final String USER = "ROLE_USER";
    public static final String ADMIN = "ROLE_ADMIN";
    private static final String BANNED = "ROLE_BANNED";
  }
}
