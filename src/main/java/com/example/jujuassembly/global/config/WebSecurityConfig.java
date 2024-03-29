package com.example.jujuassembly.global.config;

import com.example.jujuassembly.global.jwt.JwtUtil;
import com.example.jujuassembly.global.filter.CustomAccessDeniedHandler;
import com.example.jujuassembly.global.filter.CustomAuthenticationEntryPoint;
import com.example.jujuassembly.global.filter.FilterUtil;
import com.example.jujuassembly.global.filter.JwtAuthorizationFilter;
import com.example.jujuassembly.global.filter.LogoutFilter;
import com.example.jujuassembly.global.filter.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final FilterUtil filterUtil;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtAuthorizationFilter jwtAuthorizationFilter() {
    return new JwtAuthorizationFilter(jwtUtil, userDetailsService, filterUtil);
  }

  @Bean
  public LogoutFilter logoutFilter() {
    return new LogoutFilter(jwtUtil, filterUtil);
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
            .requestMatchers("/").permitAll()
            .requestMatchers("/mypage").permitAll()
            .requestMatchers("/userReview").permitAll()
            .requestMatchers("/userReport").permitAll()
            .requestMatchers("/productDetails").permitAll()
            .requestMatchers("/login").permitAll()
            .requestMatchers("/signup").permitAll()
            .requestMatchers("/products").permitAll()
            .requestMatchers("/admin/**").permitAll()
            .requestMatchers("/main/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/v1/products/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/v1/users/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/v1/categories/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/v1/notification/**").permitAll()
            .requestMatchers("/v1/auth/**").permitAll()
            .requestMatchers("/ws-connection").permitAll()
            .anyRequest().authenticated() // 그 외 모든 요청 인증처리
    );

    // 필터 관리
    http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(handler -> handler.accessDeniedHandler(customAccessDeniedHandler))
        .exceptionHandling(
            handler -> handler.authenticationEntryPoint(customAuthenticationEntryPoint));
    http.addFilterBefore(logoutFilter(), JwtAuthorizationFilter.class);

    return http.build();
  }
}
