package com.siso.integrationTest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siso.callreview.dto.request.CallReviewRequestDto;
import com.siso.integrationTest.config.IntegrationTestBase;
import com.siso.user.domain.model.*;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.request.UserProfileRequestDto;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 입력 검증 테스트
 *
 * 실무에서 중요한 입력 값 검증 시나리오를 테스트합니다.
 * - 필수 필드 누락
 * - 형식 검증 (길이, 범위)
 * - 비즈니스 규칙 검증
 */
@DisplayName("입력 검증 테스트")
class InputValidationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

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

    // ========== UserProfile 검증 테스트 ==========

    @Test
    @DisplayName("프로필 생성 시 필수 필드 누락하면 400 에러")
    void whenMissingRequiredFields_shouldReturn400() throws Exception {
        UserProfileRequestDto invalidDto = new UserProfileRequestDto(
                null, null, false, 25, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'nickname')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'location')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'sex')]").exists());
    }

    @Test
    @DisplayName("닉네임이 2자 미만이면 400 에러")
    void whenNicknameTooShort_shouldReturn400() throws Exception {
        UserProfileRequestDto invalidDto = new UserProfileRequestDto(
                null, null, false, 25, "A", "안녕하세요", "서울시 강남구",
                Sex.MALE, PreferenceSex.FEMALE, null, List.of(Meeting.HOBBY_GROUP, Meeting.CULTURE_LIFE, Meeting.TALK_CLUB)
        );

        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'nickname')].message")
                        .value("닉네임은 2자 이상 20자 이하이어야 합니다."));
    }

    @Test
    @DisplayName("닉네임이 20자 초과하면 400 에러")
    void whenNicknameTooLong_shouldReturn400() throws Exception {
        String longNickname = "A".repeat(21);
        UserProfileRequestDto invalidDto = new UserProfileRequestDto(
                null, null, false, 25, longNickname, "안녕하세요", "서울시 강남구",
                Sex.MALE, PreferenceSex.FEMALE, null, List.of(Meeting.HOBBY_GROUP, Meeting.CULTURE_LIFE, Meeting.TALK_CLUB)
        );

        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'nickname')]").exists());
    }

    @Test
    @DisplayName("나이가 19세 미만이면 400 에러")
    void whenAgeTooYoung_shouldReturn400() throws Exception {
        UserProfileRequestDto invalidDto = new UserProfileRequestDto(
                null, null, false, 18, "테스터", "안녕하세요", "서울시 강남구",
                Sex.MALE, PreferenceSex.FEMALE, null, List.of(Meeting.HOBBY_GROUP, Meeting.CULTURE_LIFE, Meeting.TALK_CLUB)
        );

        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'age')].message")
                        .value("나이는 최소 19세 이상이어야 합니다."));
    }

    @Test
    @DisplayName("나이가 100세 초과하면 400 에러")
    void whenAgeTooOld_shouldReturn400() throws Exception {
        UserProfileRequestDto invalidDto = new UserProfileRequestDto(
                null, null, false, 101, "테스터", "안녕하세요", "서울시 강남구",
                Sex.MALE, PreferenceSex.FEMALE, null, List.of(Meeting.HOBBY_GROUP, Meeting.CULTURE_LIFE, Meeting.TALK_CLUB)
        );

        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'age')].message")
                        .value("나이는 최대 100세 이하이어야 합니다."));
    }

    @Test
    @DisplayName("자기소개가 500자 초과하면 400 에러")
    void whenIntroduceTooLong_shouldReturn400() throws Exception {
        String longIntroduce = "A".repeat(501);
        UserProfileRequestDto invalidDto = new UserProfileRequestDto(
                null, null, false, 25, "테스터", longIntroduce, "서울시 강남구",
                Sex.MALE, PreferenceSex.FEMALE, null, List.of(Meeting.HOBBY_GROUP, Meeting.CULTURE_LIFE, Meeting.TALK_CLUB)
        );

        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'introduce')]").exists());
    }

    @Test
    @DisplayName("Meeting이 3개 미만이면 400 에러")
    void whenMeetingTooFew_shouldReturn400() throws Exception {
        UserProfileRequestDto invalidDto = new UserProfileRequestDto(
                null, null, false, 25, "테스터", "안녕하세요", "서울시 강남구",
                Sex.MALE, PreferenceSex.FEMALE, null, List.of(Meeting.HOBBY_GROUP, Meeting.CULTURE_LIFE)
        );

        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'meetings')]").exists());
    }

    @Test
    @DisplayName("Meeting이 7개 초과하면 400 에러")
    void whenMeetingTooMany_shouldReturn400() throws Exception {
        UserProfileRequestDto invalidDto = new UserProfileRequestDto(
                null, null, false, 25, "테스터", "안녕하세요", "서울시 강남구",
                Sex.MALE, PreferenceSex.FEMALE, null,
                List.of(Meeting.HOBBY_GROUP, Meeting.CULTURE_LIFE, Meeting.TALK_CLUB, Meeting.TEA_TIME,
                        Meeting.CLUB_ACTIVITY, Meeting.HOBBY_SHARE, Meeting.BOOK_CLUB, Meeting.FOOD_TRIP)
        );

        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'meetings')]").exists());
    }

    @Test
    @DisplayName("유효한 프로필 데이터로 요청 시 201 Created 반환")
    void whenValidProfileData_shouldReturn201() throws Exception {
        UserProfileRequestDto validDto = new UserProfileRequestDto(
                DrinkingCapacity.OCCASIONALLY, Religion.NONE, false, 25, "테스터",
                "안녕하세요. 반갑습니다.", "서울시 강남구",
                Sex.MALE, PreferenceSex.FEMALE, Mbti.ENFP,
                List.of(Meeting.HOBBY_GROUP, Meeting.CULTURE_LIFE, Meeting.TALK_CLUB)
        );

        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("테스터"))
                .andExpect(jsonPath("$.age").value(25));
    }

    // ========== CallReview 검증 테스트 ==========

    @Test
    @DisplayName("통화 리뷰 작성 시 평점 누락하면 400 에러")
    void whenRatingMissing_shouldReturn400() throws Exception {
        CallReviewRequestDto invalidDto = new CallReviewRequestDto(
                null, 1L, null, "좋았습니다"
        );

        mockMvc.perform(post("/api/call-reviews")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'rating')]").exists());
    }

    @Test
    @DisplayName("평점이 1점 미만이면 400 에러")
    void whenRatingTooLow_shouldReturn400() throws Exception {
        CallReviewRequestDto invalidDto = new CallReviewRequestDto(
                null, 1L, 0, "나빴습니다"
        );

        mockMvc.perform(post("/api/call-reviews")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'rating')].message")
                        .value("평점은 최소 1점입니다."));
    }

    @Test
    @DisplayName("평점이 5점 초과하면 400 에러")
    void whenRatingTooHigh_shouldReturn400() throws Exception {
        CallReviewRequestDto invalidDto = new CallReviewRequestDto(
                null, 1L, 6, "너무 좋았습니다"
        );

        mockMvc.perform(post("/api/call-reviews")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'rating')].message")
                        .value("평점은 최대 5점입니다."));
    }

    @Test
    @DisplayName("리뷰 내용이 500자 초과하면 400 에러")
    void whenCommentTooLong_shouldReturn400() throws Exception {
        String longComment = "A".repeat(501);
        CallReviewRequestDto invalidDto = new CallReviewRequestDto(
                null, 1L, 5, longComment
        );

        mockMvc.perform(post("/api/call-reviews")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'comment')]").exists());
    }
}
