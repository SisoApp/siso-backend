package com.siso.integrationTest.api;

import com.siso.integrationTest.config.SecuredIntegrationTestBase;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 통합 테스트
 *
 * MockMvc를 사용한 API 엔드포인트 통합 테스트
 */
@DisplayName("UserController 통합 테스트")
class UserControllerIntegrationTest extends SecuredIntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private User testUser;
    private String accessToken;

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
        accessToken = jwtTokenUtil.generateAccessToken(testUser.getEmail());
    }

    @Test
    @DisplayName("GET /api/users/info - 내 정보 조회 성공")
    void getUserInfo_shouldReturnUserDetails() throws Exception {
        mockMvc.perform(get("/api/users/info")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.provider").value("KAKAO"));
    }

    @Test
    @DisplayName("GET /api/users/info - 인증 없이 요청 시 401")
    void getUserInfo_whenNoAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users/info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/users/notification - 알림 설정 변경 성공")
    void updateNotification_shouldUpdateSettings() throws Exception {
        String requestBody = """
                {
                    "subscribed": false
                }
                """;

        mockMvc.perform(patch("/api/users/notification")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        User updatedUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();
    }

    @Test
    @DisplayName("PATCH /api/users/notification - 잘못된 요청 시 400")
    void updateNotification_withInvalidRequest_shouldReturn400() throws Exception {
        String requestBody = "{}";

        mockMvc.perform(patch("/api/users/notification")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/users/delete - 회원 탈퇴 성공")
    void deleteUser_shouldSoftDelete() throws Exception {
        mockMvc.perform(delete("/api/users/delete")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("POST /api/users/logout - 로그아웃 성공")
    void logout_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/users/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("POST /api/users/logout - 인증 없이 요청 시 401")
    void logout_whenNoAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/users/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 JWT 토큰으로 요청 시 401")
    void invalidToken_shouldReturn401() throws Exception {
        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(get("/api/users/info")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
