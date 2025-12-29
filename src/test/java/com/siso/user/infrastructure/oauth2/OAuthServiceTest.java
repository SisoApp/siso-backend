package com.siso.user.infrastructure.oauth2;

import com.siso.chat.infrastructure.OnlineUserRegistry;
import com.siso.common.exception.ExpectedException;
import com.siso.user.application.UserProfileService;
import com.siso.user.application.UserSignUpService;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.response.TokenResponseDto;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OAuthService 단위 테스트
 *
 * 테스트 대상:
 * - OAuth 로그인 성공 (신규 사용자)
 * - OAuth 로그인 성공 (기존 사용자)
 * - OAuth 로그인 실패 (이메일 없음)
 * - JWT 토큰 생성 확인
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthService 단위 테스트")
class OAuthServiceTest {

    @Mock
    private UserSignUpService userSignUpService;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuthProviderClientFactory clientFactory;

    @Mock
    private OnlineUserRegistry onlineUserRegistry;

    @Mock
    private OAuthProviderClient oAuthProviderClient;

    @InjectMocks
    private OAuthService oAuthService;

    private static final String TEST_EMAIL = "test@kakao.com";
    private static final String TEST_PHONE = "010-1234-5678";
    private static final String TEST_PROVIDER = "KAKAO";
    private static final String TEST_ACCESS_TOKEN = "mock-access-token";
    private static final String TEST_JWT_ACCESS = "jwt-access-token";
    private static final String TEST_JWT_REFRESH = "jwt-refresh-token";

    @Test
    @DisplayName("OAuth 로그인 성공 - 신규 사용자")
    void loginWithProvider_whenNewUser_shouldReturnTokens() {
        // Given: OAuth 사용자 정보
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "12345");
        attributes.put("email", TEST_EMAIL);

        OAuth2UserInfo userInfo = mock(OAuth2UserInfo.class);
        when(userInfo.getEmail()).thenReturn(TEST_EMAIL);
        when(userInfo.getPhoneNumber()).thenReturn(TEST_PHONE);

        // Given: Mock 설정
        when(clientFactory.getClient(TEST_PROVIDER)).thenReturn(oAuthProviderClient);
        when(oAuthProviderClient.getUserAttributes(TEST_ACCESS_TOKEN)).thenReturn(attributes);

        User newUser = User.builder()
                .provider(Provider.KAKAO)
                .email(TEST_EMAIL)
                .phoneNumber(TEST_PHONE)
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.PENDING)
                .build();

        when(userSignUpService.getOrCreateUser(Provider.KAKAO, TEST_EMAIL, TEST_PHONE)).thenReturn(newUser);
        when(jwtTokenUtil.generateAccessToken(TEST_EMAIL)).thenReturn(TEST_JWT_ACCESS);
        when(jwtTokenUtil.generateRefreshToken(TEST_EMAIL)).thenReturn(TEST_JWT_REFRESH);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userProfileService.existsByUserId(any())).thenReturn(false);

        // When: 로그인 시도
        TokenResponseDto result = oAuthService.loginWithProvider(TEST_PROVIDER, TEST_ACCESS_TOKEN);

        // Then: 토큰이 발급됨
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(TEST_JWT_ACCESS);
        assertThat(result.getRefreshToken()).isEqualTo(TEST_JWT_REFRESH);
        assertThat(result.getRegistrationStatus()).isEqualTo(RegistrationStatus.PENDING);
        assertThat(result.isHasProfile()).isFalse();

        // Verify: 메서드 호출 확인
        verify(clientFactory).getClient(TEST_PROVIDER);
        verify(oAuthProviderClient).getUserAttributes(TEST_ACCESS_TOKEN);
        verify(userSignUpService).getOrCreateUser(Provider.KAKAO, TEST_EMAIL, TEST_PHONE);
        verify(jwtTokenUtil).generateAccessToken(TEST_EMAIL);
        verify(jwtTokenUtil).generateRefreshToken(TEST_EMAIL);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("OAuth 로그인 성공 - 기존 사용자 (프로필 있음)")
    void loginWithProvider_whenExistingUserWithProfile_shouldReturnTokens() {
        // Given: OAuth 사용자 정보
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "12345");
        attributes.put("email", TEST_EMAIL);

        OAuth2UserInfo userInfo = mock(OAuth2UserInfo.class);
        when(userInfo.getEmail()).thenReturn(TEST_EMAIL);
        when(userInfo.getPhoneNumber()).thenReturn(TEST_PHONE);

        // Given: 기존 사용자
        User existingUser = User.builder()
                .provider(Provider.KAKAO)
                .email(TEST_EMAIL)
                .phoneNumber(TEST_PHONE)
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();

        when(clientFactory.getClient(TEST_PROVIDER)).thenReturn(oAuthProviderClient);
        when(oAuthProviderClient.getUserAttributes(TEST_ACCESS_TOKEN)).thenReturn(attributes);
        when(userSignUpService.getOrCreateUser(Provider.KAKAO, TEST_EMAIL, TEST_PHONE)).thenReturn(existingUser);
        when(jwtTokenUtil.generateAccessToken(TEST_EMAIL)).thenReturn(TEST_JWT_ACCESS);
        when(jwtTokenUtil.generateRefreshToken(TEST_EMAIL)).thenReturn(TEST_JWT_REFRESH);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userProfileService.existsByUserId(any())).thenReturn(true);

        // When: 로그인 시도
        TokenResponseDto result = oAuthService.loginWithProvider(TEST_PROVIDER, TEST_ACCESS_TOKEN);

        // Then: 토큰이 발급되고 프로필이 있음
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(TEST_JWT_ACCESS);
        assertThat(result.getRefreshToken()).isEqualTo(TEST_JWT_REFRESH);
        assertThat(result.getRegistrationStatus()).isEqualTo(RegistrationStatus.COMPLETED);
        assertThat(result.isHasProfile()).isTrue();
    }

    @Test
    @DisplayName("OAuth 로그인 실패 - 이메일 없음")
    void loginWithProvider_whenEmailIsNull_shouldThrowException() {
        // Given: 이메일이 없는 OAuth 사용자 정보
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "12345");
        // email이 없음

        when(clientFactory.getClient(TEST_PROVIDER)).thenReturn(oAuthProviderClient);
        when(oAuthProviderClient.getUserAttributes(TEST_ACCESS_TOKEN)).thenReturn(attributes);

        // When & Then: 예외 발생
        assertThatThrownBy(() -> oAuthService.loginWithProvider(TEST_PROVIDER, TEST_ACCESS_TOKEN))
                .isInstanceOf(ExpectedException.class);

        verify(clientFactory).getClient(TEST_PROVIDER);
        verify(oAuthProviderClient).getUserAttributes(TEST_ACCESS_TOKEN);
        verify(userSignUpService, never()).getOrCreateUser(any(), any(), any());
    }

    @Test
    @DisplayName("리프레시 토큰이 DB에 저장됨")
    void loginWithProvider_shouldSaveRefreshTokenToDatabase() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "12345");
        attributes.put("email", TEST_EMAIL);

        User user = User.builder()
                .provider(Provider.KAKAO)
                .email(TEST_EMAIL)
                .phoneNumber(TEST_PHONE)
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.PENDING)
                .build();

        when(clientFactory.getClient(TEST_PROVIDER)).thenReturn(oAuthProviderClient);
        when(oAuthProviderClient.getUserAttributes(TEST_ACCESS_TOKEN)).thenReturn(attributes);
        when(userSignUpService.getOrCreateUser(Provider.KAKAO, TEST_EMAIL, TEST_PHONE)).thenReturn(user);
        when(jwtTokenUtil.generateAccessToken(TEST_EMAIL)).thenReturn(TEST_JWT_ACCESS);
        when(jwtTokenUtil.generateRefreshToken(TEST_EMAIL)).thenReturn(TEST_JWT_REFRESH);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userProfileService.existsByUserId(any())).thenReturn(false);

        // When
        oAuthService.loginWithProvider(TEST_PROVIDER, TEST_ACCESS_TOKEN);

        // Then: save 메서드가 호출되어 리프레시 토큰이 저장됨
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getRefreshToken() != null &&
                savedUser.getRefreshToken().equals(TEST_JWT_REFRESH)
        ));
    }
}
