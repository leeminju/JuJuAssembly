package com.example.jujuassembly.global;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;

public interface UserTestUtil extends CategoryTestUtil {

  Long TEST_USER_ID = 1L;
  Long TEST_ANOTHER_USER_ID = 2L;
  String TEST_USER_LOGINID = "loginId";
  String TEST_USER_NICKNAME = "nickname";
  String TEST_USER_EMAIL = "email";
  String TEST_USER_PASSWORD = "password";

  Category TEST_USER_FIRSTPREFERRED_CATEGORY = TEST_CATEGORY;
  Category TEST_USER_SECONDPREFERRED_CATEGORY = TEST_ANOTHER_CATEGORY;

  User TEST_USER = User.builder()
      .id(TEST_USER_ID)
      .loginId(TEST_USER_LOGINID)
      .nickname(TEST_USER_NICKNAME)
      .email(TEST_USER_EMAIL)
      .password(TEST_USER_PASSWORD)
      .isArchived(false)
      .role(UserRoleEnum.USER)
      .firstPreferredCategory(TEST_USER_FIRSTPREFERRED_CATEGORY)
      .secondPreferredCategory(TEST_USER_SECONDPREFERRED_CATEGORY)
      .build();

  User TEST_ANOTHER_USER = User.builder()
      .id(TEST_ANOTHER_USER_ID)
      .loginId(ANOTHER_PREDIX+TEST_USER_LOGINID)
      .nickname(ANOTHER_PREDIX+TEST_USER_NICKNAME)
      .email(ANOTHER_PREDIX+TEST_USER_EMAIL)
      .password(ANOTHER_PREDIX+TEST_USER_PASSWORD)
      .isArchived(false)
      .role(UserRoleEnum.USER)
      .firstPreferredCategory(TEST_USER_FIRSTPREFERRED_CATEGORY)
      .secondPreferredCategory(TEST_USER_SECONDPREFERRED_CATEGORY)
      .build();

}
