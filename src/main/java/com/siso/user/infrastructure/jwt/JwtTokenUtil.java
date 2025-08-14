package com.siso.user.infrastructure.jwt;

import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    private final UserRepository userRepository;

    private final String SECRET_KEY = "LikeLionRocketCorpsInternship12SeniorBlindDate_siso";

    public static final long ACCESS_TOKEN_TTL = 1000 * 60 * 60 * 2; // 액세스 토큰 2시간
    public static final long REFRESH_TOKEN_TTL = 1000 * 60 * 60 * 24 * 14; // 리프레시 토큰 2주

    // 토큰에서 모든 Claims 추출
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
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
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    // 토큰 생성 로직
    private String createToken(Map<String, Object> claims, String subject, long TTL) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TTL))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

//    // 리프레시 토큰 생성 로직 (subject 없이)
//    private String createRefreshToken(long TTL) {
//        return Jwts.builder()
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + TTL))
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }

    // 액세스 토큰 생성
    public String generateAccessToken(String phoneNumber) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, phoneNumber, ACCESS_TOKEN_TTL);
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken(String phoneNumber) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, phoneNumber, REFRESH_TOKEN_TTL);
    }

//    // 토큰 유효성 검사
//    public Boolean validateToken(String token, String phoneNumber) {
//        final String extractedPhoneNumber = extractPhoneNumber(token);
//        return extractedPhoneNumber.equals(phoneNumber) && !isTokenExpired(token);
//    }

    // 쿠키 생성
    public Cookie createCookie(String key, String value, boolean isRefreshToken) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge((int) REFRESH_TOKEN_TTL / 1000); // 초 단위로 설정
        cookie.setSecure(true); // HTTPS 통신 시에만 쿠키 전송
        cookie.setHttpOnly(true); // XSS 공격 방지
        cookie.setPath("/"); // 모든 경로에서 쿠키 접근 가능

        if (isRefreshToken) {
            cookie.setPath("/api/auth/refresh"); // 리프레시 토큰은 특정 경로에서만 사용
        }
        return cookie;
    }

    // 액세스 토큰 삭제용 쿠키
    public Cookie accessTokenRemover() {
        Cookie accessToken = new Cookie("accessToken", null);
        accessToken.setMaxAge(0);
        accessToken.setSecure(true);
        accessToken.setPath("/");
        accessToken.setHttpOnly(true);
        return accessToken;
    }

    // 리프레시 토큰을 DB에 저장
    public void storeRefreshToken(String phoneNumber, String newRefreshToken){
        User user = userRepository.findActiveUserByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        user.updateRefreshToken(newRefreshToken);
        userRepository.save(user);
    }
}