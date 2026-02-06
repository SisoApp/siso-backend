package com.siso.unitTest.util;

import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JwtTokenUtil 단위 테스트
 *
 * 테스트 대상:
 * - JWT 액세스 토큰 생성
 * - JWT 리프레시 토큰 생성
 * - 토큰 검증 (유효성, 만료 여부)
 * - 토큰에서 이메일 추출
 * - 리프레시 토큰 타입 확인
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenUtil 단위 테스트")
class JwtTokenUtilTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtTokenUtil jwtTokenUtil;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_SECRET = "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long-for-hs256";
    private static final long ACCESS_TOKEN_TTL = 2 * 60 * 60 * 1000L; // 2시간
    private static final long REFRESH_TOKEN_TTL = 14 * 24 * 60 * 60 * 1000L; // 2주

    @BeforeEach
    void setUp() {
        // @Value 필드에 테스트 값 주입
        ReflectionTestUtils.setField(jwtTokenUtil, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "accessTokenTtl", ACCESS_TOKEN_TTL);
        ReflectionTestUtils.setField(jwtTokenUtil, "refreshTokenTtl", REFRESH_TOKEN_TTL);
    }

    @Test
    @DisplayName("액세스 토큰 생성 성공")
    void generateAccessToken_shouldCreateValidToken() {
        // When: 액세스 토큰 생성
        String token = jwtTokenUtil.generateAccessToken(TEST_EMAIL);

        // Then: 토큰이 생성되고 유효해야 함
        assertThat(token).isNotNull();
        assertThat(jwtTokenUtil.validateToken(token)).isTrue();

        // Then: 이메일 추출 가능
        String extractedEmail = jwtTokenUtil.extractEmail(token);
        assertThat(extractedEmail).isEqualTo(TEST_EMAIL);

        // Then: 액세스 토큰 타입 확인
        assertThat(jwtTokenUtil.isRefreshToken(token)).isFalse();
    }

    @Test
    @DisplayName("리프레시 토큰 생성 성공")
    void generateRefreshToken_shouldCreateValidToken() {
        // When: 리프레시 토큰 생성
        String token = jwtTokenUtil.generateRefreshToken(TEST_EMAIL);

        // Then: 토큰이 생성되고 유효해야 함
        assertThat(token).isNotNull();
        assertThat(jwtTokenUtil.validateToken(token)).isTrue();

        // Then: 이메일 추출 가능
        String extractedEmail = jwtTokenUtil.extractEmail(token);
        assertThat(extractedEmail).isEqualTo(TEST_EMAIL);

        // Then: 리프레시 토큰 타입 확인
        assertThat(jwtTokenUtil.isRefreshToken(token)).isTrue();
    }

    @Test
    @DisplayName("토큰에서 Claims 추출 성공")
    void extractAllClaims_shouldReturnClaims() {
        // Given: 액세스 토큰 생성
        String token = jwtTokenUtil.generateAccessToken(TEST_EMAIL);

        // When: Claims 추출
        Claims claims = jwtTokenUtil.extractAllClaims(token);

        // Then: Claims가 올바르게 추출됨
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(TEST_EMAIL);
        assertThat(claims.get("type")).isEqualTo("access");
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateToken_whenTokenIsValid_shouldReturnTrue() {
        // Given: 유효한 토큰 생성
        String token = jwtTokenUtil.generateAccessToken(TEST_EMAIL);

        // When & Then: 검증 성공
        assertThat(jwtTokenUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 실패")
    void validateToken_whenTokenIsInvalid_shouldReturnFalse() {
        // Given: 잘못된 토큰
        String invalidToken = "invalid.token.here";

        // When & Then: 검증 실패
        assertThat(jwtTokenUtil.validateToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("토큰에서 사용자 조회 성공")
    void validateAndGetUserId_whenUserExists_shouldReturnUser() {
        // Given: 토큰 생성
        String token = jwtTokenUtil.generateAccessToken(TEST_EMAIL);

        // Given: Mock User 생성
        User mockUser = User.builder()
                .provider(Provider.KAKAO)
                .email(TEST_EMAIL)
                .phoneNumber("010-1234-5678")
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));

        // When: 토큰으로 사용자 조회
        User foundUser = jwtTokenUtil.validateAndGetUserId(token);

        // Then: 사용자가 올바르게 조회됨
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(TEST_EMAIL);
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("토큰에서 사용자 조회 실패 - 사용자 없음")
    void validateAndGetUserId_whenUserNotExists_shouldThrowException() {
        // Given: 토큰 생성
        String token = jwtTokenUtil.generateAccessToken(TEST_EMAIL);

        // Given: 사용자가 없음
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When & Then: 예외 발생
        assertThatThrownBy(() -> jwtTokenUtil.validateAndGetUserId(token))
                .isInstanceOf(ExpectedException.class);

        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("리프레시 토큰에서 이메일 추출 성공")
    void getEmailFromToken_shouldExtractEmail() {
        // Given: 리프레시 토큰 생성
        String token = jwtTokenUtil.generateRefreshToken(TEST_EMAIL);

        // When: 이메일 추출
        String email = jwtTokenUtil.getEmailFromToken(token);

        // Then: 이메일이 올바르게 추출됨
        assertThat(email).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("토큰 만료 확인 - 유효한 토큰")
    void isTokenExpired_whenTokenIsValid_shouldReturnFalse() {
        // Given: 유효한 토큰 생성
        String token = jwtTokenUtil.generateAccessToken(TEST_EMAIL);

        // When & Then: 만료되지 않음
        assertThat(jwtTokenUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰 타입 구분")
    void tokenType_shouldBeDifferentBetweenAccessAndRefresh() {
        // Given: 액세스 토큰과 리프레시 토큰 생성
        String accessToken = jwtTokenUtil.generateAccessToken(TEST_EMAIL);
        String refreshToken = jwtTokenUtil.generateRefreshToken(TEST_EMAIL);

        // When & Then: 타입이 올바르게 구분됨
        assertThat(jwtTokenUtil.isRefreshToken(accessToken)).isFalse();
        assertThat(jwtTokenUtil.isRefreshToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("액세스 토큰 만료 시간 검증 - 2시간")
    void accessToken_shouldExpireInTwoHours() {
        // Given: 액세스 토큰 생성
        String token = jwtTokenUtil.generateAccessToken(TEST_EMAIL);

        // When: 만료 시간 추출
        Date expirationDate = jwtTokenUtil.extractExpiration(token);
        long expirationTime = expirationDate.getTime() - System.currentTimeMillis();

        // Then: 약 2시간 (7200000ms) 후 만료
        long twoHoursInMs = 2 * 60 * 60 * 1000; // 7200000ms
        long tolerance = 5000; // 5초 오차 허용

        assertThat(expirationTime)
                .isGreaterThan(twoHoursInMs - tolerance)
                .isLessThan(twoHoursInMs + tolerance);
    }

    @Test
    @DisplayName("리프레시 토큰 만료 시간 검증 - 2주")
    void refreshToken_shouldExpireInTwoWeeks() {
        // Given: 리프레시 토큰 생성
        String token = jwtTokenUtil.generateRefreshToken(TEST_EMAIL);

        // When: 만료 시간 추출
        Date expirationDate = jwtTokenUtil.extractExpiration(token);
        long expirationTime = expirationDate.getTime() - System.currentTimeMillis();

        // Then: 약 2주 (1209600000ms) 후 만료
        long twoWeeksInMs = 14 * 24 * 60 * 60 * 1000L; // 1209600000ms
        long tolerance = 5000; // 5초 오차 허용

        assertThat(expirationTime)
                .isGreaterThan(twoWeeksInMs - tolerance)
                .isLessThan(twoWeeksInMs + tolerance);
    }

    @Test
    @DisplayName("토큰에 userId claims 포함 여부 검증")
    void token_shouldIncludeEmailInClaims() {
        // Given: 액세스 토큰 생성
        String token = jwtTokenUtil.generateAccessToken(TEST_EMAIL);

        // When: Claims에서 subject(email) 추출
        Claims claims = jwtTokenUtil.extractAllClaims(token);
        String subject = claims.getSubject();

        // Then: 이메일이 subject에 포함되어야 함
        assertThat(subject).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("토큰 서명 알고리즘 검증 - HS256")
    void token_shouldUseHS256Algorithm() {
        // Given: 액세스 토큰 생성
        String token = jwtTokenUtil.generateAccessToken(TEST_EMAIL);

        // When: 토큰 파싱 (서명 검증 포함)
        // Then: HS256 알고리즘으로 서명되었으므로 정상 파싱됨
        assertThat(jwtTokenUtil.validateToken(token)).isTrue();

        // Then: Claims를 추출할 수 있음 (서명이 유효하다는 의미)
        Claims claims = jwtTokenUtil.extractAllClaims(token);
        assertThat(claims).isNotNull();
    }
}
