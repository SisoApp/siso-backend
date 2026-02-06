package com.siso.integrationTest.api;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.domain.repository.CallRepository;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CallQualityController 통합 테스트")
class CallQualityControllerIntegrationTest extends SecuredIntegrationTestBase {

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

        testCall = Call.builder()
                .caller(testUser)
                .receiver(otherUser)
                .callStatus(CallStatus.ACCEPT)
                .startTime(LocalDateTime.now())
                .agoraChannelName("test-channel")
                .agoraToken("test-token")
                .build();

        testCall = callRepository.save(testCall);

        accessToken = jwtTokenUtil.generateAccessToken(testUser.getEmail());
    }

    @Test
    @DisplayName("POST /api/call-quality/metrics - 통화 품질 메트릭 제출 성공")
    void submitCallQualityMetrics_shouldSucceed() throws Exception {
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

        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/call-quality/metrics - Call ID 누락 시 400")
    void submitCallQualityMetrics_whenCallIdMissing_shouldReturn400() throws Exception {
        String requestBody = """
                {
                    "packetLossRate": 2,
                    "jitter": 50,
                    "roundTripTime": 100
                }
                """;

        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/call-quality/metrics - 패킷 손실률 범위 초과 시 400")
    void submitCallQualityMetrics_whenPacketLossRateOutOfRange_shouldReturn400() throws Exception {
        String requestBody = String.format("""
                {
                    "callId": %d,
                    "packetLossRate": 150,
                    "jitter": 50,
                    "roundTripTime": 100
                }
                """, testCall.getId());

        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/call-quality/metrics - 인증 없이 요청 시 401")
    void submitCallQualityMetrics_whenNoAuthentication_shouldReturn401() throws Exception {
        String requestBody = String.format("""
                {
                    "callId": %d,
                    "packetLossRate": 2,
                    "jitter": 50,
                    "roundTripTime": 100
                }
                """, testCall.getId());

        mockMvc.perform(post("/api/call-quality/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/call-quality/metrics - 존재하지 않는 통화 ID로 요청 시 404")
    void submitCallQualityMetrics_whenCallNotExists_shouldReturn404() throws Exception {
        Long nonExistentCallId = 999L;
        String requestBody = String.format("""
                {
                    "callId": %d,
                    "packetLossRate": 2,
                    "jitter": 50,
                    "roundTripTime": 100
                }
                """, nonExistentCallId);

        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/call-quality/metrics/{callId} - 통화 품질 조회 성공")
    void getCallQualityMetrics_shouldSucceed() throws Exception {
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

        mockMvc.perform(get("/api/call-quality/metrics/{callId}", testCall.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/call-quality/metrics/{callId} - 인증 없이 요청 시 401")
    void getCallQualityMetrics_whenNoAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/call-quality/metrics/{callId}", testCall.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/call-quality/poor-quality - 품질 나쁜 통화 조회 성공")
    void getPoorQualityCalls_shouldSucceed() throws Exception {
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

        mockMvc.perform(get("/api/call-quality/poor-quality")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/call-quality/average - 평균 품질 통계 조회 성공")
    void getAverageQualityMetrics_shouldSucceed() throws Exception {
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

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

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
        mockMvc.perform(get("/api/call-quality/average")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/call-quality/average - 인증 없이 요청 시 401")
    void getAverageQualityMetrics_whenNoAuthentication_shouldReturn401() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        mockMvc.perform(get("/api/call-quality/average")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
