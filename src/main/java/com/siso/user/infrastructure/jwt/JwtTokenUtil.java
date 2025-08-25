package com.siso.user.infrastructure.jwt;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
    private final UserRepository userRepository;
    // 토큰 만료 시간
    public static final long ACCESS_TOKEN_TTL = 1000 * 60 * 60 * 2;       // 액세스 토큰 2시간
    public static final long REFRESH_TOKEN_TTL = 1000 * 60 * 60 * 24 * 14; // 리프레시 토큰 2주

    // ----------------------
    // Claims 추출
    // ----------------------
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY_OBJECT)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 이메일 추출
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    // ----------------------
    // RefreshToken 타입 확인
    // ----------------------
    public boolean isRefreshToken(String token) {
        String type = extractClaim(token, claims -> claims.get("type", String.class));
        return "refresh".equals(type);
    }

    // refreshToken에서 email(subject) 추출
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY_OBJECT)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject(); // JWT subject에 email 저장했다고 가정
    }

    // ----------------------
    // 토큰 생성
    // ----------------------
    public String generateAccessToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims, email, ACCESS_TOKEN_TTL);
    }

    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, email, REFRESH_TOKEN_TTL);
    }

    private String createToken(Map<String, Object> claims, String subject, long ttl) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)       // 이메일을 subject로 설정
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ttl))
                .signWith(SECRET_KEY_OBJECT, SignatureAlgorithm.HS256)
                .compact();
    }

    // ----------------------
    // 유효성 검증
    // ----------------------
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY_OBJECT).build().parseClaimsJws(token);
            return !isTokenExpired(token); // 만료 여부까지 체크
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 토큰 검증 후 userId 반환
     * 유효하지 않으면 예외 발생
     */
    public Long validateAndGetUserId(String token) {
        // 토큰에서 이메일 가져오기
        String email = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY_OBJECT)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        // 이메일로 User 조회 후 userId 반환
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND))
                .getId();
    }
}