package com.example.jujuassembly.global.jwt;

import com.example.jujuassembly.global.exception.ApiException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

  private final RedisTemplate<String, String> redisTemplate;

  // Header KEY 값
  public static final String AUTHORIZATION_HEADER = "Authorization";

  // Token 식별자
  public static final String BEARER_PREFIX = "Bearer ";
  public static final String ACCESS_PREFIX = "Access ";

  public static final long ACCESS_TOKEN_TIME =  60*1000;  // 15분

  public static final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000;  // 7일

  public static final long EXPIRE_TOKEN_TIME = 0;

  @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
  private String secretKey;

  private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

  private Key key;


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

  public void saveAccessToken(String token, String loginId) {
    redisTemplate.opsForValue()
        .set(ACCESS_PREFIX+token, loginId, ACCESS_TOKEN_TIME, TimeUnit.MILLISECONDS);
  }

  public void saveRefreshToken(String tokenValue, String refreshToken) {
    redisTemplate.opsForValue()
        .set(tokenValue, refreshToken, REFRESH_TOKEN_TIME, TimeUnit.MILLISECONDS);
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


  // 헤더에서 LoginId 가져오기
  public String getLoginIdFromHeader(HttpServletRequest request) {
    String tokenValue = resolveToken(request);
    Claims info = getUserInfoFromToken(tokenValue);
    return info.getSubject();
  }

  public boolean shouldAccessTokenBeRefreshed(String accessTokenValue) {
    return !redisTemplate.hasKey(ACCESS_PREFIX+BEARER_PREFIX+accessTokenValue);
  }

  public String getRefreshtokenValue(String accessTokenValue) {
    return redisTemplate.opsForValue().get(accessTokenValue);
  }

  public String createAccessTokenByRefreshToken(String refreshTokenValue) {
    String loginId = getUserInfoFromToken(refreshTokenValue).getSubject();
    return createAccessToken(loginId);
  }

  // logout시 refresh token 만료시키기
  public void removeRefreshToken(String accessTokenValue) {
    if (redisTemplate.opsForValue().get(accessTokenValue).isEmpty()) {
      throw new ApiException("AccessToken이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
    }
    redisTemplate.delete(accessTokenValue);
  }

  // logout시 access token 만료시키기
  public void removeAccessToken(String accessToken) {
    if (redisTemplate.opsForValue().get(ACCESS_PREFIX+accessToken).isEmpty()) {
      throw new ApiException("AccessToken이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
    }
    redisTemplate.delete(ACCESS_PREFIX+accessToken);
  }


  public void regenerateToken(String newAccessToken, String accessTokenValue, String refreshTokenValue) {
    Claims info = getUserInfoFromToken(newAccessToken.substring(7));
    String loginId = info.getSubject();

    // 새로 만든 AccessToken을 redis에 저장
    redisTemplate.opsForValue().set(ACCESS_PREFIX+newAccessToken, loginId, ACCESS_TOKEN_TIME, TimeUnit.MILLISECONDS);

    // 새로 만든 AccessToken을 key로 refreshToken을 다시 DB에 저장
    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshTokenValue).getBody();
    Long expireationTime = claims.getExpiration().getTime();
    Long currentTime = System.currentTimeMillis();
    redisTemplate.opsForValue().set(newAccessToken.substring(7), BEARER_PREFIX+refreshTokenValue, expireationTime - currentTime, TimeUnit.MILLISECONDS);

    // 만료된 token으로 저장되어있는 refreshToken은 삭제
    redisTemplate.delete(accessTokenValue);
  }
}
