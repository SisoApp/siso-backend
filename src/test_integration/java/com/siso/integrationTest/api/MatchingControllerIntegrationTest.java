package com.siso.integrationTest.api;

import com.siso.integrationTest.config.SecuredIntegrationTestBase;
import com.siso.matching.dto.MatchingResultDto;
import com.siso.user.domain.model.*;
import com.siso.user.domain.repository.UserProfileRepository;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("MatchingController 통합 테스트")
class MatchingControllerIntegrationTest extends SecuredIntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RedisTemplate<String, MatchingResultDto> redisTemplate;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private User testUser;
    private UserProfile testProfile;
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

        testProfile = UserProfile.builder()
                .user(testUser)
                .nickname("테스트닉네임")
                .age(25)
                .sex(Sex.MALE)
                .mbti(Mbti.ENFP)
                .preferenceSex(PreferenceSex.FEMALE)
                .drinkingCapacity(DrinkingCapacity.OCCASIONALLY)
                .religion(Religion.NONE)
                .smoke(false)
                .location("서울")
                .introduce("안녕하세요")
                .meetings(Arrays.asList(Meeting.CLUB_ACTIVITY, Meeting.HOBBY_GROUP))
                .build();
        userProfileRepository.save(testProfile);

        accessToken = jwtTokenUtil.generateAccessToken(testUser.getEmail());
    }

    @Test
    @DisplayName("POST /api/matching/request - AI 매칭 요청 성공")
    void requestMatching_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/matching/request")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/matching/request - 인증 없이 요청 시 401")
    void requestMatching_whenNoAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/matching/request")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/matching/results - 매칭 결과 조회 성공")
    void getMatchingResults_shouldSucceed() throws Exception {
        String cacheKey = "matching:" + testUser.getId();

        MatchingResultDto.UserMatchScore matchScore = new MatchingResultDto.UserMatchScore(
                2L, "매칭사용자", 24, "INFP",
                Arrays.asList("영화", "음악"),
                "https://example.com/profile.jpg", 0.85
        );

        List<MatchingResultDto.UserMatchScore> matches = Arrays.asList(matchScore);

        MatchingResultDto resultDto = new MatchingResultDto(
                testUser.getId(), matches, LocalDateTime.now(), 10
        );

        redisTemplate.opsForValue().set(cacheKey, resultDto, 30, TimeUnit.MINUTES);

        mockMvc.perform(get("/api/matching/results")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.matches").isArray())
                .andExpect(jsonPath("$.matches[0].candidateId").value(2))
                .andExpect(jsonPath("$.matches[0].matchScore").value(0.85))
                .andExpect(jsonPath("$.totalCandidates").value(10));
    }

    @Test
    @DisplayName("GET /api/matching/results - 매칭 결과 없음 시 404")
    void getMatchingResults_whenNoResults_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/matching/results")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/matching/results - 인증 없이 요청 시 401")
    void getMatchingResults_whenNoAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/matching/results")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("매칭 요청 후 결과 조회 플로우")
    void matchingFlow_requestAndGetResults() throws Exception {
        String requestResponse = mockMvc.perform(post("/api/matching/request")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String cacheKey = "matching:" + testUser.getId();

        MatchingResultDto.UserMatchScore matchScore1 = new MatchingResultDto.UserMatchScore(
                2L, "매칭사용자1", 24, "INFP",
                Arrays.asList("영화", "음악"),
                "https://example.com/profile1.jpg", 0.85
        );

        MatchingResultDto.UserMatchScore matchScore2 = new MatchingResultDto.UserMatchScore(
                3L, "매칭사용자2", 26, "ENFJ",
                Arrays.asList("운동", "여행"),
                "https://example.com/profile2.jpg", 0.78
        );

        List<MatchingResultDto.UserMatchScore> matches = Arrays.asList(matchScore1, matchScore2);

        MatchingResultDto resultDto = new MatchingResultDto(
                testUser.getId(), matches, LocalDateTime.now(), 15
        );

        redisTemplate.opsForValue().set(cacheKey, resultDto, 30, TimeUnit.MINUTES);

        mockMvc.perform(get("/api/matching/results")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").isArray())
                .andExpect(jsonPath("$.matches.length()").value(2))
                .andExpect(jsonPath("$.matches[0].matchScore").value(0.85))
                .andExpect(jsonPath("$.matches[1].matchScore").value(0.78))
                .andExpect(jsonPath("$.totalCandidates").value(15));
    }
}
