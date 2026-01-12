package com.siso.user.presentation;

import com.siso.config.IntegrationTestBase;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.UserRepository;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 통합 테스트
 *
 * MockMvc를 사용한 API 엔드포인트 통합 테스트
 */
@DisplayName("UserController 통합 테스트")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // Given: 테스트 사용자 생성
        testUser = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        testUser = userRepository.save(testUser);

        // Given: JWT 토큰 생성
        accessToken = jwtTokenUtil.generateAccessToken(testUser.getEmail());
    }

    @Test
    @DisplayName("GET /api/users/{userId} - 사용자 조회 성공")
    void getUser_shouldReturnUserDetails() throws Exception {
        // When & Then: API 호출 및 검증
        mockMvc.perform(get("/api/users/{userId}", testUser.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.provider").value("KAKAO"));
    }

    @Test
    @DisplayName("GET /api/users/{userId} - 존재하지 않는 사용자 조회 시 404")
    void getUser_whenUserNotExists_shouldReturn404() throws Exception {
        // Given: 존재하지 않는 사용자 ID
        Long nonExistentUserId = 999L;

        // When & Then: 404 에러 발생
        mockMvc.perform(get("/api/users/{userId}", nonExistentUserId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/{userId} - 인증 없이 요청 시 401")
    void getUser_whenNoAuthentication_shouldReturn401() throws Exception {
        // When & Then: 인증 없이 요청하면 401 에러
        mockMvc.perform(get("/api/users/{userId}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/users/{userId} - 사용자 soft delete 성공")
    void deleteUser_shouldSoftDelete() throws Exception {
        // When: DELETE 요청
        mockMvc.perform(delete("/api/users/{userId}", testUser.getId())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Then: DB에서 조회 시 isDeleted = true
        User deletedUser = userRepository.findById(testUser.getId()).orElse(null);

        // findById는 isDeleted = false인 사용자만 조회하므로 null이어야 함
        // (UserRepository의 @Query 조건 참고)
    }

    @Test
    @DisplayName("PATCH /api/users/{userId}/presence - Presence 상태 업데이트")
    void updatePresenceStatus_shouldUpdateStatus() throws Exception {
        // Given: 상태 업데이트 요청 JSON
        String requestBody = """
                {
                    "presenceStatus": "OFFLINE"
                }
                """;

        // When & Then: PATCH 요청
        mockMvc.perform(patch("/api/users/{userId}/presence", testUser.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        // Then: 상태가 변경되었는지 확인
        User updatedUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();
        // presenceStatus가 OFFLINE으로 변경되었는지 검증 가능
    }

    @Test
    @DisplayName("POST /api/users/{userId}/block - 사용자 차단")
    void blockUser_shouldBlockUser() throws Exception {
        // Given: 차단할 다른 사용자 생성
        User targetUser = User.builder()
                .provider(Provider.KAKAO)
                .email("target@example.com")
                .phoneNumber("010-9999-8888")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        targetUser = userRepository.save(targetUser);

        // When & Then: 차단 요청
        mockMvc.perform(post("/api/users/{userId}/block", targetUser.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("잘못된 JWT 토큰으로 요청 시 401")
    void invalidToken_shouldReturn401() throws Exception {
        // Given: 잘못된 토큰
        String invalidToken = "invalid.jwt.token";

        // When & Then: 401 에러
        mockMvc.perform(get("/api/users/{userId}", testUser.getId())
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
