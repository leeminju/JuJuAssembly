package com.example.jujuassembly.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

  private final RedisTemplate<String, String> redisTemplate;

//  private final TokenRepository tokenRepository;
//
//  public JwtUtil(TokenRepository tokenRepository) {
//    this.tokenRepository = tokenRepository;
//  }

  // Header KEY 값
  public static final String AUTHORIZATION_HEADER = "Authorization";

  // Token 식별자
  public static final String BEARER_PREFIX = "Bearer ";

  public static final long ACCESS_TOKEN_TIME = 15 * 60 * 1000;  // 15분

  public static final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000;  // 7일

  @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
  private String secretKey;

  private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

  private Key key;

//  private final String adminToken = "관리자 비밀번호";


  @PostConstruct
  public void init() {
    byte[] bytes = Base64.getDecoder().decode(secretKey);
    key = Keys.hmacShaKeyFor(bytes);
  }

  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(7);
    }
    return null;
  }


  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
    } catch (ExpiredJwtException e) {
      log.error("Expired JWT token, 만료된 JWT token 입니다.");
    } catch (UnsupportedJwtException e) {
      log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
    } catch (IllegalArgumentException e) {
      log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
    }
    return false;
  }

  public Claims getUserInfoFromToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  public String createAccessToken(String loginId) {
    return createToken(loginId, ACCESS_TOKEN_TIME);
  }

  public String createRefreshToken(String loginId) {
    return createToken(loginId, REFRESH_TOKEN_TIME);
  }

  public void saveRefreshToken(String accessToken, String refreshToken) {
    redisTemplate.opsForValue()
        .set(accessToken, refreshToken, REFRESH_TOKEN_TIME, TimeUnit.MINUTES);
  }

  private String createToken(String loginId, long tokenTime) {
    Date date = new Date();

    return BEARER_PREFIX +
        Jwts.builder()
            .setSubject(loginId)
            .setExpiration(new Date(date.getTime() + tokenTime))
            .setIssuedAt(date)
            .signWith(key, signatureAlgorithm)
            .compact();
  }

//  public boolean validateAdminPW(String PW) {
//    return adminToken.equals(PW);
//  }


  // 헤더에서 사용자 이름 가져오기
  public String getUsernameFromHeader(HttpServletRequest request) {
    //String tokenValue = getJwtFromHeader(request);
    String tokenValue = resolveToken(request);
    Claims info = getUserInfoFromToken(tokenValue);
    return info.getSubject();
  }

  public boolean shouldAccessTokenBeRefreshed(String accessToken) {
    try {
      Date expirationDate = Jwts.parserBuilder().setSigningKey(key).build()
          .parseClaimsJws(accessToken).getBody().getExpiration();

      // 토큰의 만료 여부 확인
      return expirationDate.before(new Date());
    } catch (SecurityException | MalformedJwtException e) {
      log.error("Invalid JWT signature, 유효하지 않는 Access JWT 서명 입니다.");
      return false;
    } catch (ExpiredJwtException e) {
      log.error("Expired JWT token, 만료된 Access JWT token 입니다.");
      return true;
    } catch (UnsupportedJwtException e) {
      log.error("Unsupported Access JWT token, 지원되지 않는 Access JWT 토큰 입니다.");
      return false;
    } catch (IllegalArgumentException e) {
      log.error("Access JWT claims is empty, 잘못된 Access JWT 토큰 입니다.");
      return false;
    }
  }

  public String getRefreshtokenValue(String accessTokenValue) {
    return redisTemplate.opsForValue().get(accessTokenValue);
  }

  public String createAccessTokenByRefreshToken(String refreshToken) {
    String loginId = getUserInfoFromToken(refreshToken).getSubject();
    return createAccessToken(loginId);
  }

//  // 발행된 토큰을 테이블에서 만료
//  @Transactional
//  @Scheduled(fixedRate = 60 * 1000)  // 1분에 한번 작동
//  public void cleanupExpireTokens() {
//    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
//    tokenRepository.deleteByCreatedTimeBefore(oneHourAgo);  // 1시간 이전 발행된 모든 토큰 삭제
//  }


}
