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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

  private final RedisTemplate<String, String> redisTemplate;

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


  @PostConstruct
  public void init() {
    byte[] bytes = Base64.getDecoder().decode(secretKey);
    key = Keys.hmacShaKeyFor(bytes);
  }

  public String getTokenFromRequest(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
          try {
            return URLDecoder.decode(cookie.getValue(), "UTF-8");
          } catch (Exception e) {
            return null;
          }
        }
      }
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

  public void saveAccessTokenByLoginId(String loginId, String accessToken) {
    redisTemplate.opsForValue()
        .set(loginId, accessToken, REFRESH_TOKEN_TIME, TimeUnit.MILLISECONDS);
  }

  public void saveRefreshTokenByAccessToken(String accessToken, String refreshToken) {
    redisTemplate.opsForValue()
        .set(accessToken, refreshToken, REFRESH_TOKEN_TIME, TimeUnit.MILLISECONDS);
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


  public boolean shouldAccessTokenBeRefreshed(String accessTokenValue) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessTokenValue);
      return false;
    } catch (SecurityException | MalformedJwtException e) {
      log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
      return false;
    } catch (ExpiredJwtException e) {
      log.error("Expired JWT token, 만료된 JWT token 입니다.");
      return true;
    } catch (UnsupportedJwtException e) {
      log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
      return false;
    } catch (IllegalArgumentException e) {
      log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
      return false;
    }
  }

  public String getRefreshtokenByAccessToken(String accessToken) {
    return redisTemplate.opsForValue().get(accessToken);
  }

  public String createAccessTokenByRefreshToken(String refreshTokenValue) {
    String loginId = getUserInfoFromToken(refreshTokenValue).getSubject();
    return createAccessToken(loginId);
  }

  // logout시 refresh token 만료시키기
  public void removeRefreshToken(String accessToken) {
    if (!redisTemplate.hasKey(accessToken)) {
      throw new ApiException("RefreshToken이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
    }
    redisTemplate.delete(accessToken);
  }

  // logout시 access token 만료시키기
  public void removeAccessToken(String accessToken) {
    Claims claims = getUserInfoFromToken(accessToken.substring(7));
    String loginId = claims.getSubject();
    if (!redisTemplate.hasKey(loginId)) {
      throw new ApiException("AccessToken이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
    }
    redisTemplate.delete(loginId);
  }


  public void regenerateToken(String newAccessToken, String accessToken,
      String refreshTokenValue) {
    Claims info = getUserInfoFromToken(refreshTokenValue);
    String loginId = info.getSubject();

    Long expirationTime = info.getExpiration().getTime();

    // 새로 만든 AccessToken을 redis에 저장
    redisTemplate.opsForValue()
        .set(loginId, newAccessToken,
            expirationTime, TimeUnit.MILLISECONDS);

    // 새로 만든 AccessToken을 key로 refreshToken을 다시 DB에 저장
    redisTemplate.opsForValue().set(newAccessToken,
        BEARER_PREFIX + refreshTokenValue,
        expirationTime, TimeUnit.MILLISECONDS);

    // 만료된 token으로 저장되어있는 refreshToken은 삭제
    redisTemplate.delete(accessToken);
  }

  public void checkIsLoggedIn(String loginId, HttpServletResponse response) {
    if (redisTemplate.hasKey(loginId)) {
      response.setHeader(AUTHORIZATION_HEADER, redisTemplate.opsForValue().get(loginId));
      throw new ApiException("이미 로그인 되어있습니다.", HttpStatus.BAD_REQUEST);
    }
  }

  public boolean checkIsLoggedOut(String accessToken) {
    return !redisTemplate.hasKey(accessToken);
  }

  public String createExpiredToken(String accessToken) {
    String loginId = getUserInfoFromToken(accessToken.substring(7)).getSubject();
    return createToken(loginId, 0);
  }

  public Cookie addJwtToCookie(String bearerAccessToken) {
    try {
      String spaceRemovedToken = URLEncoder.encode(bearerAccessToken, "utf-8")
          .replaceAll("\\+", "%20"); // 공백 제거

      Cookie cookie = new Cookie(AUTHORIZATION_HEADER, spaceRemovedToken);
      cookie.setMaxAge((int) ACCESS_TOKEN_TIME);
      cookie.setPath("/");

      return cookie;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
