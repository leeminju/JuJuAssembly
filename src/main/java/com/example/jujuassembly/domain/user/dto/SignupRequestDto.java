package com.example.jujuassembly.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequestDto {

  @NotBlank(message = "ID를 입력하세요.")
  @Pattern(regexp = "^[a-z0-9]{6,15}$", message = "ID는 영어 소문자 + 숫자 6자 이상 15자 이하로 입력해주세요")
  private String loginId;

  @NotBlank(message = "닉네임을 입력하세요.")
  @Pattern(regexp = "^[a-z0-9가-힣]{3,20}$", message = "닉네임은 영어 소문자, 한글, 숫자 조합으로 3자 이상 20자 이하 입력해주세요.")
  private String nickname;

  @NotBlank(message = "PW를 입력하세요.")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\\$%^&*()+\\-=\\[\\]{};:\",./<>?\\\\`~])[a-zA-Z\\d!@#\\$%^&*()+\\-=\\[\\]{};:\",./<>?\\\\`~]{8,20}$",
      message = "PW는 8자 ~20자 이내로 영문 대소문자, 숫자, 특수문자(’,”,_,| 제외 )를 모두 사용하여 입력해주세요")
  private String password;
  private String passwordCheck;

  private Long firstPreferredCategoryId;
  private Long secondPreferredCategoryId;

  @NotBlank(message = "email을 입력하세요.")
  @Email(message = "email 형식이 올바르지 않습니다.")
  private String email;

}
