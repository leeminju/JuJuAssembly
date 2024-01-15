package com.example.jujuassembly.global.config;


import com.example.jujuassembly.global.jwt.JwtUtil;
import com.example.jujuassembly.global.security.CustomAuthenticationEntryPoint;
import com.example.jujuassembly.global.security.JwtAuthorizationFilter;
import com.example.jujuassembly.global.security.UserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;
  private final AccessDeniedHandler customAccessDeniedHandler;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
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
            .requestMatchers("/").permitAll()
            .requestMatchers("/login").permitAll()
            .requestMatchers("/signup").permitAll()
            .requestMatchers("/admin").permitAll()
            .requestMatchers(HttpMethod.GET, "/v1/users/**").permitAll()
            .requestMatchers("/v1/auth/**").permitAll()
            .requestMatchers("/v1/categories/**").permitAll() //이후 수정
            .requestMatchers("/view/**").permitAll() //이후 수정
            .anyRequest().authenticated() // 그 외 모든 요청 인증처리
    );

 /*   // 필터 관리
    http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(handler -> handler.accessDeniedHandler(customAccessDeniedHandler))
        .exceptionHandling(
            handler -> handler.authenticationEntryPoint(customAuthenticationEntryPoint));
*/
    return http.build();
  }
}