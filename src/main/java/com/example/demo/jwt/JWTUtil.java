package com.example.demo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    // application.yml에 설정된 문자열 secret을 JWT 서명에 사용할 SecretKey로 변환한다.
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // JWT payload에서 로그인 식별자인 이메일을 추출한다.
    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    // JWT payload에서 Spring Security 권한 문자열을 추출한다.
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // JWT의 만료 시간이 현재 시간보다 이전인지 확인한다.
    public boolean isTokenExpired(String token) {
        return parseClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // 이메일, 권한, 만료 시간을 담아 클라이언트에 전달할 JWT를 생성한다.
    public String createJwt(String email, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // JWT 서명과 만료 시간을 검증한 뒤 payload Claims를 반환한다.
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
