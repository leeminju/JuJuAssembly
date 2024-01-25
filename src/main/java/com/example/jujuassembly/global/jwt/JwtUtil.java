package com.example.jujuassembly.global.jwt;

import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.filter.FilterUtil;
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
import java.io.IOException;
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
  private final FilterUtil filterUtil;

  // Header KEY 값
  public static final String AUTHORIZATION_HEADER = "Authorization";

  // Token 식별자
  public static final String BEARER_PREFIX = "Bearer ";

  public static final long ACCESS_TOKEN_TIME =  1000;  // 15분

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
    } catch (Exception e) {
      log.error("token validattion 에러");
    }
    return false;
  }

  public Claims getUserInfoFromToken(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    } catch (Exception ex) {
      throw new ApiException("토큰에서 유저 정보 조회 실패", HttpStatus.BAD_REQUEST);
    }
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
    } catch (ExpiredJwtException e) {
      log.error("Expired JWT token, 만료된 JWT token 입니다.");
      return true;
    } catch (Exception e) {
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

  public void removeTokensAtRedisDB(String accessToken, HttpServletResponse response)
      throws IOException {
    String refreshToken = redisTemplate.opsForValue().get(accessToken);
    Claims info = getUserInfoFromToken(refreshToken.substring(7));
    String loginId = info.getSubject();
    if (!redisTemplate.hasKey(loginId) || !redisTemplate.hasKey(accessToken)) {
      filterUtil.setMassageToResponse("redis에 해당 access token이 존재하지 않음.", response);
    }
    redisTemplate.delete(accessToken);
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

  public Boolean checkIsLoggedIn(String loginId) {
    if (redisTemplate.hasKey(loginId)) {
      return true;
    }
    return false;
  }

  public String getAccessTokenByLoginId(String loginId) {
    return redisTemplate.opsForValue().get(loginId);
  }

  // 세션 쿠키(브라우저 종료 시점에 쿠키 삭제) 생성
  public Cookie addJwtToCookie(String bearerAccessToken) {
    try {
      String spaceRemovedToken = URLEncoder.encode(bearerAccessToken, "utf-8")
          .replaceAll("\\+", "%20"); // 공백 제거

      Cookie cookie = new Cookie(AUTHORIZATION_HEADER, spaceRemovedToken);
      cookie.setPath("/");

      return cookie;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  // 로그아웃시 사용되는 쿠키 만료용
  public Cookie createExpiredCookie(String key, String value) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    return cookie;
  }

}
