package com.example.jujuassembly.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequestDto {

  @NotBlank(message = "ID를 입력하세요.")
  @Pattern(regexp = "^[a-zA-Z0-9]{6,}$", message = "ID는 영어 + 숫자 6자 이상")
  private String loginId;

  @NotBlank(message = "닉네임을 입력하세요.")
  @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "닉네임은 영어 + 숫자로 입력")
  private String nickname;

  @NotBlank(message = "PW를 입력하세요.")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\\$%^&*()+\\-=\\[\\]{};:\",./<>?\\\\`~])[a-zA-Z\\d!@#\\$%^&*()+\\-=\\[\\]{};:\",./<>?\\\\`~]{8,20}$",
      message = "PW는 8자 ~20자 이내로 영문 대소문자, 숫자, 특수문자(’,”,_,| 제외 )를 모두 사용하여 구성")
  private String password;
  private String passwordCheck;

  private Long firstPreferredCategoryId;
  private Long secondPreferredCategoryId;

  @NotBlank(message = "email을 입력하세요.")
  @Email(message = "email 형식이 올바르지 않습니다.")
  private String email;

}
