package com.example.jujuassembly.global.filter;


import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.global.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;
  private final FilterUtil filterUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String accessToken = jwtUtil.getTokenFromRequest(request);

    if (Objects.nonNull(accessToken)) {

      UserDetailsImpl userDetails;

      String accessTokenValue = accessToken.substring(7);

      // accessToken이 만료되었는지 확인
      if (jwtUtil.shouldAccessTokenBeRefreshed(accessToken.substring(7))) {
        String refreshTokenValue = jwtUtil.getRefreshtokenByAccessToken(accessToken).substring(7);
        // refreshtoken이 유효한지 확인
        if (jwtUtil.validateToken(refreshTokenValue)) {
          // accessToken 재발급
          String newAccessToken = jwtUtil.createAccessTokenByRefreshToken(refreshTokenValue);
          Cookie cookie = jwtUtil.addJwtToCookie(newAccessToken);
          response.addCookie(cookie);

          // DB 토큰도 새로고침
          jwtUtil.regenerateToken(newAccessToken, accessToken, refreshTokenValue);

          // 재발급된 토큰으로 검증 진행하도록 대입
          accessTokenValue = newAccessToken.substring(7);
        }
        // 유효하지 않다면 재발급 없이 만료된 상태로 진행
      }

      if (jwtUtil.validateToken(accessTokenValue)) {
        Claims info = jwtUtil.getUserInfoFromToken(accessTokenValue);

        // 인증정보에 유저정보(username) 넣기
        // username -> user 조회
        String username = info.getSubject();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        // -> userDetails 에 담고
        userDetails = userDetailsService.getUserDetailsImpl(username);
        // -> authentication의 principal 에 담고
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());
        // -> securityContent 에 담고
        context.setAuthentication(authentication);
        // -> SecurityContextHolder 에 담고
        SecurityContextHolder.setContext(context);
        // -> 이제 @AuthenticationPrincipal 로 조회할 수 있음
      } else {
        // 인증정보가 존재하지 않을때
        filterUtil.setMassageToResponse("인가 불가: 토큰이 유효하지 않습니다.", response);
        return;
      }

      // BANNED 유저 확인
      if (userDetails != null) {
        if (userDetails.getUser().getRole().equals(UserRoleEnum.BANNED)) {
          filterUtil.setMassageToResponse("BANNED USER", response);
          return;
        }
      }

      // 회원 탈퇴한 유저인지 확인
      if (userDetails != null) {
        if (userDetails.getUser().getIsArchived()) {
          filterUtil.setMassageToResponse("이미 회원 탈퇴한 유저입니다.", response);
          return;
        }
      }

    }

    filterChain.doFilter(request, response);
  }


}