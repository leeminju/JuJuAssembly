package com.example.jujuassembly.global.security;

import com.example.jujuassembly.global.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j(topic = "AccessTokenManagementFilter")
@RequiredArgsConstructor
public class LogoutFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final FilterUtil filterUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String accessToken = jwtUtil.getTokenFromRequest(request);

    // 로그아웃 요청일 때는 재발급하지 않음
    String uri = request.getRequestURI();
    if (uri.equals("/v1/users/logout")) {

      if (accessToken == null) {
        filterUtil.setMassageToResponse("토큰이 존재하지 않습니다.", response);
        return;
      }

      // redis token 삭제
      jwtUtil.removeTokensAtRedisDB(accessToken, response);

      // 쿠키 만료 시키기
      Cookie cookie = jwtUtil.createExpiredCookie(JwtUtil.AUTHORIZATION_HEADER, "logged-out");
      response.addCookie(cookie);

      filterUtil.setMassageToResponse("로그아웃 되었습니다.", response);
      return;
    }

    filterChain.doFilter(request, response);
  }

}
