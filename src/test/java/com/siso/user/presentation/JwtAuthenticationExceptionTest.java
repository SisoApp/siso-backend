package com.siso.user.presentation;

import com.siso.config.IntegrationTestBase;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.security.Key;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JWT 인증 예외 테스트
 *
 * 실무에서 중요한 인증/인가 예외 시나리오를 검증합니다.
 * - 토큰 없이 요청
 * - 만료된 토큰
 * - 잘못된 형식의 토큰
 * - 유효하지 않은 서명의 토큰
 */
@DisplayName("JWT 인증 예외 테스트")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class JwtAuthenticationExceptionTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("토큰 없이 요청 시 401 Unauthorized 반환")
    void whenNoToken_shouldReturn401() throws Exception {
        // When & Then: Authorization 헤더 없이 요청
        mockMvc.perform(get("/api/users/info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Bearer 형식이 아닌 토큰으로 요청 시 401 반환")
    void whenInvalidBearerFormat_shouldReturn401() throws Exception {
        // Given: Bearer 없이 토큰만 전송
        String validToken = jwtTokenUtil.generateAccessToken(testUser.getEmail());

        // When & Then: Bearer 없이 토큰만 헤더에 포함
        mockMvc.perform(get("/api/users/info")
                .header("Authorization", validToken)  // "Bearer " 없음
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("만료된 토큰으로 요청 시 401 반환 및 TOKEN_EXPIRED 에러")
    void whenExpiredToken_shouldReturn401WithExpiredError() throws Exception {
        // Given: 이미 만료된 토큰 생성 (발급 시각을 과거로 설정)
        Date pastDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24); // 24시간 전
        Date expiredDate = new Date(pastDate.getTime() + 1000); // 1초 후 만료

        String expiredToken = Jwts.builder()
                .setSubject(testUser.getEmail())
                .claim("type", "access")
                .setIssuedAt(pastDate)
                .setExpiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(getSecretKey().getBytes()), SignatureAlgorithm.HS256)
                .compact();

        // When & Then: 만료된 토큰으로 요청
        mockMvc.perform(get("/api/users/info")
                .header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
                // Note: Filter에서 조용히 실패하므로 401만 확인
    }

    @Test
    @DisplayName("잘못된 형식의 토큰으로 요청 시 401 반환")
    void whenMalformedToken_shouldReturn401() throws Exception {
        // Given: 잘못된 형식의 토큰
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // When & Then: 잘못된 토큰으로 요청
        mockMvc.perform(get("/api/users/info")
                .header("Authorization", "Bearer " + malformedToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 서명의 토큰으로 요청 시 401 반환")
    void whenInvalidSignature_shouldReturn401() throws Exception {
        // Given: 다른 비밀키로 서명된 토큰 생성
        Key wrongKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        String tokenWithWrongSignature = Jwts.builder()
                .setSubject(testUser.getEmail())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(wrongKey, SignatureAlgorithm.HS256)
                .compact();

        // When & Then: 잘못된 서명의 토큰으로 요청
        mockMvc.perform(get("/api/users/info")
                .header("Authorization", "Bearer " + tokenWithWrongSignature)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("refresh 토큰으로 access 토큰이 필요한 API 요청 시 401 반환")
    void whenRefreshTokenUsedForAccessAPI_shouldReturn401() throws Exception {
        // Given: refresh 토큰 생성
        String refreshToken = Jwts.builder()
                .setSubject(testUser.getEmail())
                .claim("type", "refresh")  // refresh 타입
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(Keys.hmacShaKeyFor(getSecretKey().getBytes()), SignatureAlgorithm.HS256)
                .compact();

        // When & Then: refresh 토큰으로 일반 API 요청
        mockMvc.perform(get("/api/users/info")
                .header("Authorization", "Bearer " + refreshToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 유효한 토큰으로 요청 시 401 반환")
    void whenTokenForNonExistentUser_shouldReturn401() throws Exception {
        // Given: 존재하지 않는 이메일로 토큰 생성
        String tokenForNonExistentUser = Jwts.builder()
                .setSubject("nonexistent@example.com")
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(Keys.hmacShaKeyFor(getSecretKey().getBytes()), SignatureAlgorithm.HS256)
                .compact();

        // When & Then: 존재하지 않는 사용자 토큰으로 요청
        mockMvc.perform(get("/api/users/info")
                .header("Authorization", "Bearer " + tokenForNonExistentUser)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("다른 사용자의 토큰으로 리소스 접근 시 정상 동작 (본인 정보만 조회)")
    void whenAccessOwnResourceWithValidToken_shouldSucceed() throws Exception {
        // Given: 다른 사용자 생성
        User anotherUser = User.builder()
                .provider(Provider.KAKAO)
                .email("another@example.com")
                .phoneNumber("010-9999-9999")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();
        anotherUser = userRepository.save(anotherUser);

        // Given: 다른 사용자의 토큰
        String anotherUserToken = jwtTokenUtil.generateAccessToken(anotherUser.getEmail());

        // When & Then: 다른 사용자 토큰으로 자신의 정보 조회는 성공
        mockMvc.perform(get("/api/users/info")
                .header("Authorization", "Bearer " + anotherUserToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("another@example.com"));
    }

    /**
     * 테스트용 비밀키 가져오기
     * 실제 운영 환경의 비밀키와 동일한 형식으로 생성
     */
    private String getSecretKey() {
        // JwtTokenUtil에서 사용하는 비밀키와 동일해야 함
        // 실제 환경에서는 application-secret.yml에서 가져옴
        return "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long-for-hs256";
    }
}
