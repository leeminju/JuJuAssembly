package com.example.jujuassembly.global.config;


import com.example.jujuassembly.global.jwt.JwtUtil;
import com.example.jujuassembly.global.security.JwtAuthorizationFilter;
import com.example.jujuassembly.global.security.UserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final JwtUtil jwtUtil;

  private final UserDetailsService userDetailsService;

  private final ObjectMapper objectMapper;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtAuthorizationFilter jwtAuthorizationFilter() {
    return new JwtAuthorizationFilter(jwtUtil, userDetailsService, objectMapper);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // CSRF 설정
    http.csrf((csrf) -> csrf.disable());

    // 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
    http.sessionManagement((sessionManagement) ->
        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

    http.authorizeHttpRequests((authorizeHttpRequests) ->
        authorizeHttpRequests
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
            .permitAll() // resources 접근 허용 설정
            .requestMatchers("/v1/auth/**").permitAll()
            .anyRequest().authenticated() // 그 외 모든 요청 인증처리
    );

    // 필터 관리
    http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

    // 접근불가 페이지 생성시 활용
//        http.exceptionHandling(config -> {
//            config.authenticationEntryPoint(errorPoint());
//            config.accessDeniedHandler(accessDeniedHandler());
//        });

    return http.build();
  }

  private AccessDeniedHandler accessDeniedHandler() {
    return (request, response, ex) -> {
      response.setStatus(400);
    };
  }

  private AuthenticationEntryPoint errorPoint() {
    return (request, response, authException) -> {
      authException.printStackTrace();
    };
  }
}