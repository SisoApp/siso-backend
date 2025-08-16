package com.siso.user.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    private static final String SECRET_KEY = "LikeLionRocketCorpsInternship12SeniorBlindDate_siso";
    private static final SecretKey SECRET_KEY_OBJECT = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());


    public static final long ACCESS_TOKEN_TTL = 1000 * 60 * 60 * 2; // 액세스 토큰 2시간
    public static final long REFRESH_TOKEN_TTL = 1000 * 60 * 60 * 24 * 14; // 리프레시 토큰 2주

    // 토큰에서 모든 Claims 추출
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 특정 Claim 추출
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 토큰에서 사용자 식별자(전화번호) 추출
    public String extractPhoneNumber(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 토큰에서 만료 시간 추출
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 토큰 만료 여부 확인
    public Boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    // 액세스 토큰 생성 (전화번호 포함)
    public String generateAccessToken(String phoneNumber) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, phoneNumber, ACCESS_TOKEN_TTL);
    }

    // 리프레시 토큰 생성 (고유 식별자만 포함)
    public String generateRefreshToken() {
        return Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_TTL))
                .signWith(SECRET_KEY_OBJECT, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 생성 공통 로직
    public String createToken(Map<String, Object> claims, String subject, long ttl) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ttl))
                .signWith(SECRET_KEY_OBJECT, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검사 (서명 및 만료 여부)
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}