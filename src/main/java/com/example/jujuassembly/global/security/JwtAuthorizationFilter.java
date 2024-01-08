package com.example.jujuassembly.global.security;


import com.example.jujuassembly.global.jwt.JwtUtil;
import com.example.jujuassembly.global.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    String accessTokenValue = jwtUtil.resolveToken(request);

    if (Objects.nonNull(accessTokenValue)) {

      // accessToken이 만료되었는지 확인
      if (jwtUtil.shouldAccessTokenBeRefreshed(accessTokenValue)) {
        String refreshTokenValue = jwtUtil.getRefreshtokenValue(accessTokenValue);
        // refreshtoken이 유효한지 확인
        if (jwtUtil.validateToken(refreshTokenValue)) {
          // accessToken 재발급
          String accessToken = jwtUtil.createAccessTokenByRefreshToken(refreshTokenValue);
          response.setHeader(JwtUtil.AUTHORIZATION_HEADER, accessTokenValue);
          accessTokenValue = accessToken.substring(7);
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
        UserDetailsImpl userDetails = userDetailsService.getUserDetailsImpl(username);
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
        ApiResponse apiResponse = new ApiResponse("토큰이 유효하지 않습니다.", HttpStatus.BAD_REQUEST.value());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        return;//return을 해줘야 결과 출력
      }
    }

    filterChain.doFilter(request, response);
  }


}