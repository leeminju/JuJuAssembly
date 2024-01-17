package com.example.jujuassembly.domain.user.kakao;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoUserInfoDto {
    private Long id;
    private String nickname;
    private String email;
    private String imageUrl;

}