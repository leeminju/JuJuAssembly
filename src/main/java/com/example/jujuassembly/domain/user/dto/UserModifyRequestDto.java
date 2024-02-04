package com.example.jujuassembly.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
public class UserModifyRequestDto {

  @NotBlank(message = "닉네임을 입력하세요.")
  @Pattern(regexp = "^[a-z0-9가-힣]{3,20}$", message = "닉네임은 영어 소문자, 한글, 숫자 조합으로 3자 이상 20자 이하 입력해주세요.")
  private String nickname;

  @JsonInclude(Include.NON_DEFAULT)
  private String currentPassword;

  @JsonInclude(Include.NON_DEFAULT)
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\\$%^&*()+\\-=\\[\\]{};:\",./<>?\\\\`~])[a-zA-Z\\d!@#\\$%^&*()+\\-=\\[\\]{};:\",./<>?\\\\`~]{8,20}$",
      message = "PW는 8자 ~20자 이내로 영문 대소문자, 숫자, 특수문자(’,”,_,| 제외 )를 모두 사용하여 입력해주세요")
  private String password;

  @JsonInclude(Include.NON_DEFAULT)
  private String passwordCheck;

  private Long firstPreferredCategoryId;

  private Long secondPreferredCategoryId;

}
