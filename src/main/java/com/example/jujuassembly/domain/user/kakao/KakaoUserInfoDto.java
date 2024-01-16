package com.example.jujuassembly.domain.user.kakao;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfoDto {
    private Long id;
    private String nickname;
    private String email;
    private String imageUrl;

    public KakaoUserInfoDto(Long id, String nickname, String email, String imageUrl) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.imageUrl = imageUrl;
    }

}