package com.siso.call.presentation;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallQualityMetrics;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.domain.repository.CallRepository;
import com.siso.config.IntegrationTestBase;
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
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CallQualityController 통합 테스트")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CallQualityControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CallRepository callRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private User testUser;
    private User otherUser;
    private Call testCall;
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

        otherUser = User.builder()
                .provider(Provider.KAKAO)
                .email("other@example.com")
                .phoneNumber("010-9999-8888")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        testUser = userRepository.save(testUser);
        otherUser = userRepository.save(otherUser);

        // Given: 테스트 통화 생성
        testCall = Call.builder()
                .caller(testUser)
                .receiver(otherUser)
                .callStatus(CallStatus.ACCEPT)
                .startTime(LocalDateTime.now())
                .agoraChannelName("test-channel")
                .agoraToken("test-token")
                .build();

        testCall = callRepository.save(testCall);

        // Given: JWT 토큰 생성
        accessToken = jwtTokenUtil.generateAccessToken(testUser.getEmail());
    }

    @Test
    @DisplayName("POST /api/call-quality/metrics - 통화 품질 메트릭 제출 성공")
    void submitCallQualityMetrics_shouldSucceed() throws Exception {
        // Given: 통화 품질 메트릭 요청 JSON
        String requestBody = String.format("""
                {
                    "callId": %d,
                    "packetLossRate": 2,
                    "jitter": 50,
                    "roundTripTime": 100,
                    "audioBitrate": 64,
                    "videoBitrate": 512,
                    "audioCodec": "opus",
                    "videoCodec": "vp8",
                    "clientType": "iOS",
                    "networkType": "WiFi"
                }
                """, testCall.getId());

        // When & Then: 메트릭 제출
        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/call-quality/metrics - Call ID 누락 시 400")
    void submitCallQualityMetrics_whenCallIdMissing_shouldReturn400() throws Exception {
        // Given: Call ID가 누락된 요청
        String requestBody = """
                {
                    "packetLossRate": 2,
                    "jitter": 50,
                    "roundTripTime": 100
                }
                """;

        // When & Then: 400 에러
        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/call-quality/metrics - 패킷 손실률 범위 초과 시 400")
    void submitCallQualityMetrics_whenPacketLossRateOutOfRange_shouldReturn400() throws Exception {
        // Given: 패킷 손실률이 100을 초과하는 요청
        String requestBody = String.format("""
                {
                    "callId": %d,
                    "packetLossRate": 150,
                    "jitter": 50,
                    "roundTripTime": 100
                }
                """, testCall.getId());

        // When & Then: 400 에러
        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/call-quality/metrics - 인증 없이 요청 시 401")
    void submitCallQualityMetrics_whenNoAuthentication_shouldReturn401() throws Exception {
        // Given: 통화 품질 메트릭 요청 JSON
        String requestBody = String.format("""
                {
                    "callId": %d,
                    "packetLossRate": 2,
                    "jitter": 50,
                    "roundTripTime": 100
                }
                """, testCall.getId());

        // When & Then: 인증 없이 요청하면 401 에러
        mockMvc.perform(post("/api/call-quality/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/call-quality/metrics - 존재하지 않는 통화 ID로 요청 시 404")
    void submitCallQualityMetrics_whenCallNotExists_shouldReturn404() throws Exception {
        // Given: 존재하지 않는 통화 ID
        Long nonExistentCallId = 999L;
        String requestBody = String.format("""
                {
                    "callId": %d,
                    "packetLossRate": 2,
                    "jitter": 50,
                    "roundTripTime": 100
                }
                """, nonExistentCallId);

        // When & Then: 404 에러
        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/call-quality/metrics/{callId} - 통화 품질 조회 성공")
    void getCallQualityMetrics_shouldSucceed() throws Exception {
        // Given: 먼저 품질 메트릭을 제출
        String submitRequest = String.format("""
                {
                    "callId": %d,
                    "packetLossRate": 2,
                    "jitter": 50,
                    "roundTripTime": 100,
                    "audioBitrate": 64,
                    "audioCodec": "opus",
                    "clientType": "iOS",
                    "networkType": "WiFi"
                }
                """, testCall.getId());

        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(submitRequest));

        // When & Then: 메트릭 조회
        mockMvc.perform(get("/api/call-quality/metrics/{callId}", testCall.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/call-quality/metrics/{callId} - 인증 없이 요청 시 401")
    void getCallQualityMetrics_whenNoAuthentication_shouldReturn401() throws Exception {
        // When & Then: 인증 없이 요청하면 401 에러
        mockMvc.perform(get("/api/call-quality/metrics/{callId}", testCall.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/call-quality/poor-quality - 품질 나쁜 통화 조회 성공")
    void getPoorQualityCalls_shouldSucceed() throws Exception {
        // Given: 품질이 나쁜 메트릭 제출
        String poorQualityRequest = String.format("""
                {
                    "callId": %d,
                    "packetLossRate": 10,
                    "jitter": 200,
                    "roundTripTime": 500
                }
                """, testCall.getId());

        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(poorQualityRequest));

        // When & Then: 품질 나쁜 통화 조회
        mockMvc.perform(get("/api/call-quality/poor-quality")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/call-quality/average - 평균 품질 통계 조회 성공")
    void getAverageQualityMetrics_shouldSucceed() throws Exception {
        // Given: 메트릭 제출
        String requestBody = String.format("""
                {
                    "callId": %d,
                    "packetLossRate": 2,
                    "jitter": 50,
                    "roundTripTime": 100
                }
                """, testCall.getId());

        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // Given: 날짜 범위 설정
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When & Then: 평균 품질 통계 조회
        mockMvc.perform(get("/api/call-quality/average")
                .header("Authorization", "Bearer " + accessToken)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/call-quality/average - 날짜 파라미터 누락 시 400")
    void getAverageQualityMetrics_whenDateMissing_shouldReturn400() throws Exception {
        // When & Then: 날짜 파라미터 없이 요청하면 400 에러
        mockMvc.perform(get("/api/call-quality/average")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/call-quality/average - 인증 없이 요청 시 401")
    void getAverageQualityMetrics_whenNoAuthentication_shouldReturn401() throws Exception {
        // Given: 날짜 범위 설정
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When & Then: 인증 없이 요청하면 401 에러
        mockMvc.perform(get("/api/call-quality/average")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
