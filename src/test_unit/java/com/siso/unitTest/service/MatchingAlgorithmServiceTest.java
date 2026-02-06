package com.siso.unitTest.service;

import com.siso.matching.application.service.MatchingAlgorithmService;
import com.siso.user.domain.model.*;
import com.siso.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AI 매칭 알고리즘 단위 테스트
 *
 * 6가지 스코어 계산 알고리즘 각각을 독립적으로 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI 매칭 알고리즘 단위 테스트")
class MatchingAlgorithmServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchingAlgorithmService matchingAlgorithmService;

    // ===================== 1. 관심사 유사도 테스트 (30%) =====================
    // Note: 관심사 유사도는 User 엔티티의 UserInterest 컬렉션을 필요로 하므로
    // 통합 테스트(MatchingAlgorithmIntegrationTest)에서 테스트합니다.

    // ===================== 2. 나이 호환성 테스트 (20%) =====================

    @Test
    @DisplayName("나이 호환성 - 나이 차이 0살일 때 1.0")
    void ageCompatibility_whenSameAge_shouldReturn1() {
        // Given: 같은 나이의 두 프로필
        UserProfile userProfile = createUserProfile(25);
        UserProfile candidateProfile = createUserProfile(25);

        // When: 나이 호환성 계산
        double score = matchingAlgorithmService.calculateAgeCompatibility(userProfile, candidateProfile);

        // Then: 점수 1.0
        assertThat(score).isEqualTo(1.0, within(0.001));
    }

    @Test
    @DisplayName("나이 호환성 - 나이 차이 5살일 때 0.5")
    void ageCompatibility_when5YearsDifference_shouldReturn0Point5() {
        // Given: 나이 차이 5살
        UserProfile userProfile = createUserProfile(25);
        UserProfile candidateProfile = createUserProfile(30);

        // When: 나이 호환성 계산
        double score = matchingAlgorithmService.calculateAgeCompatibility(userProfile, candidateProfile);

        // Then: 점수 0.5 (1.0 - 5/10)
        assertThat(score).isEqualTo(0.5, within(0.001));
    }

    @Test
    @DisplayName("나이 호환성 - 나이 차이 10살 이상일 때 0.0")
    void ageCompatibility_when10YearsOrMore_shouldReturn0() {
        // Given: 나이 차이 10살
        UserProfile userProfile = createUserProfile(25);
        UserProfile candidateProfile = createUserProfile(35);

        // When: 나이 호환성 계산
        double score = matchingAlgorithmService.calculateAgeCompatibility(userProfile, candidateProfile);

        // Then: 점수 0.0
        assertThat(score).isEqualTo(0.0, within(0.001));
    }

    // ===================== 3. MBTI 호환성 테스트 (15%) =====================

    @Test
    @DisplayName("MBTI 호환성 - 완벽한 궁합일 때 1.0")
    void mbtiCompatibility_whenPerfectMatch_shouldReturn1() {
        // Given: ENFP와 INTJ (완벽한 궁합)
        String mbti1 = "ENFP";
        String mbti2 = "INTJ";

        // When: MBTI 호환성 계산
        double score = matchingAlgorithmService.calculateMbtiCompatibility(mbti1, mbti2);

        // Then: 점수 1.0
        assertThat(score).isEqualTo(1.0, within(0.001));
    }

    @Test
    @DisplayName("MBTI 호환성 - 같은 MBTI일 때 0.8")
    void mbtiCompatibility_whenSameMbti_shouldReturn0Point8() {
        // Given: 같은 MBTI
        String mbti1 = "ENFP";
        String mbti2 = "ENFP";

        // When: MBTI 호환성 계산
        double score = matchingAlgorithmService.calculateMbtiCompatibility(mbti1, mbti2);

        // Then: 점수 0.8
        assertThat(score).isEqualTo(0.8, within(0.001));
    }

    @Test
    @DisplayName("MBTI 호환성 - 2글자 일치할 때 0.3")
    void mbtiCompatibility_when2CharsMatch_shouldReturn0Point3() {
        // Given: 2글자 일치 (E_F_)
        String mbti1 = "ENFP";
        String mbti2 = "ESFJ";

        // When: MBTI 호환성 계산
        double score = matchingAlgorithmService.calculateMbtiCompatibility(mbti1, mbti2);

        // Then: 점수 0.3 (2 * 0.15)
        assertThat(score).isEqualTo(0.3, within(0.001));
    }

    @Test
    @DisplayName("MBTI 호환성 - null일 때 0.5 (중립)")
    void mbtiCompatibility_whenNull_shouldReturn0Point5() {
        // Given: MBTI 정보 없음
        String mbti1 = null;
        String mbti2 = "ENFP";

        // When: MBTI 호환성 계산
        double score = matchingAlgorithmService.calculateMbtiCompatibility(mbti1, mbti2);

        // Then: 점수 0.5 (중립)
        assertThat(score).isEqualTo(0.5, within(0.001));
    }

    // ===================== 4. 지역 근접성 테스트 (15%) =====================

    @Test
    @DisplayName("지역 근접성 - 같은 도시일 때 1.0")
    void locationProximity_whenSameCity_shouldReturn1() {
        // Given: 같은 도시
        String location1 = "서울";
        String location2 = "서울";

        // When: 지역 근접성 계산
        double score = matchingAlgorithmService.calculateLocationProximity(location1, location2);

        // Then: 점수 1.0
        assertThat(score).isEqualTo(1.0, within(0.001));
    }

    @Test
    @DisplayName("지역 근접성 - 같은 광역시/도일 때 0.7")
    void locationProximity_whenSameRegion_shouldReturn0Point7() {
        // Given: 같은 광역시/도 (서울 강남 vs 서울 종로)
        String location1 = "서울 강남구";
        String location2 = "서울 종로구";

        // When: 지역 근접성 계산
        double score = matchingAlgorithmService.calculateLocationProximity(location1, location2);

        // Then: 점수 0.7
        assertThat(score).isEqualTo(0.7, within(0.001));
    }

    @Test
    @DisplayName("지역 근접성 - 다른 지역일 때 0.3")
    void locationProximity_whenDifferentRegion_shouldReturn0Point3() {
        // Given: 다른 광역시/도
        String location1 = "서울";
        String location2 = "부산";

        // When: 지역 근접성 계산
        double score = matchingAlgorithmService.calculateLocationProximity(location1, location2);

        // Then: 점수 0.3
        assertThat(score).isEqualTo(0.3, within(0.001));
    }

    @Test
    @DisplayName("지역 근접성 - null일 때 0.5 (중립)")
    void locationProximity_whenNull_shouldReturn0Point5() {
        // Given: 지역 정보 없음
        String location1 = null;
        String location2 = "서울";

        // When: 지역 근접성 계산
        double score = matchingAlgorithmService.calculateLocationProximity(location1, location2);

        // Then: 점수 0.5 (중립)
        assertThat(score).isEqualTo(0.5, within(0.001));
    }

    // ===================== 5. 활동성 점수 테스트 (10%) =====================

    @Test
    @DisplayName("활동성 점수 - 1일 이내 접속 시 1.0")
    void activityScore_whenActiveWithin1Day_shouldReturn1() {
        // Given: 1시간 전 접속
        LocalDateTime lastActiveAt = LocalDateTime.now().minusHours(1);

        // When: 활동성 점수 계산
        double score = matchingAlgorithmService.calculateActivityScore(lastActiveAt);

        // Then: 점수 1.0에 가까움
        assertThat(score).isGreaterThan(0.95);
    }

    @Test
    @DisplayName("활동성 점수 - 3일 전 접속 시 낮은 점수")
    void activityScore_when3DaysAgo_shouldReturnLowScore() {
        // Given: 3일 전 접속 (72시간 전)
        LocalDateTime lastActiveAt = LocalDateTime.now().minusDays(3);

        // When: 활동성 점수 계산
        double score = matchingAlgorithmService.calculateActivityScore(lastActiveAt);

        // Then: 점수 0.0 (24시간 이상)
        assertThat(score).isEqualTo(0.0, within(0.001));
    }

    @Test
    @DisplayName("활동성 점수 - 12시간 전 접속 시 0.5")
    void activityScore_when12HoursAgo_shouldReturn0Point5() {
        // Given: 12시간 전 접속
        LocalDateTime lastActiveAt = LocalDateTime.now().minusHours(12);

        // When: 활동성 점수 계산
        double score = matchingAlgorithmService.calculateActivityScore(lastActiveAt);

        // Then: 점수 0.5 (1.0 - 12/24)
        assertThat(score).isEqualTo(0.5, within(0.001));
    }

    @Test
    @DisplayName("활동성 점수 - null일 때 0.0")
    void activityScore_whenNull_shouldReturn0() {
        // Given: 접속 시간 정보 없음
        LocalDateTime lastActiveAt = null;

        // When: 활동성 점수 계산
        double score = matchingAlgorithmService.calculateActivityScore(lastActiveAt);

        // Then: 점수 0.0
        assertThat(score).isEqualTo(0.0, within(0.001));
    }

    // ===================== 6. 생활습관 호환성 테스트 (10%) =====================

    @Test
    @DisplayName("생활습관 호환성 - 음주/흡연 모두 일치 시 1.0")
    void lifestyleCompatibility_whenFullMatch_shouldReturn1() {
        // Given: 음주/흡연 모두 일치
        UserProfile userProfile = createUserProfileWithLifestyle(DrinkingCapacity.FREQUENTLY, false);
        UserProfile candidateProfile = createUserProfileWithLifestyle(DrinkingCapacity.FREQUENTLY, false);

        // When: 생활습관 호환성 계산
        double score = matchingAlgorithmService.calculateLifestyleCompatibility(userProfile, candidateProfile);

        // Then: 점수 1.0
        assertThat(score).isEqualTo(1.0, within(0.001));
    }

    @Test
    @DisplayName("생활습관 호환성 - 흡연만 일치, 음주 차이 큼")
    void lifestyleCompatibility_whenOnlySmokingMatches_shouldReturn0Point75() {
        // Given: 흡연만 일치, 음주는 차이 큼
        UserProfile userProfile = createUserProfileWithLifestyle(DrinkingCapacity.NEVER, false);
        UserProfile candidateProfile = createUserProfileWithLifestyle(DrinkingCapacity.FREQUENTLY, false);

        // When: 생활습관 호환성 계산
        double score = matchingAlgorithmService.calculateLifestyleCompatibility(userProfile, candidateProfile);

        // Then: 점수 0.75 (음주 0.25 + 흡연 0.5)
        // NEVER(ordinal=2) vs FREQUENTLY(ordinal=0) = 차이 2
        // 음주 점수: (1.0 - 2 * 0.25) * 0.5 = 0.25
        // 흡연 점수: 0.5 (일치)
        assertThat(score).isEqualTo(0.75, within(0.001));
    }

    @Test
    @DisplayName("생활습관 호환성 - 음주 1단계 차이 시 0.875")
    void lifestyleCompatibility_when1StepDrinkDifference_shouldReturn0Point875() {
        // Given: 음주 1단계 차이, 흡연 일치
        UserProfile userProfile = createUserProfileWithLifestyle(DrinkingCapacity.FREQUENTLY, false);
        UserProfile candidateProfile = createUserProfileWithLifestyle(DrinkingCapacity.OCCASIONALLY, false);

        // When: 생활습관 호환성 계산
        double score = matchingAlgorithmService.calculateLifestyleCompatibility(userProfile, candidateProfile);

        // Then: 점수 0.875 (음주 0.375 + 흡연 0.5)
        assertThat(score).isEqualTo(0.875, within(0.001));
    }

    // ===================== Helper Methods =====================

    private UserProfile createUserProfile(int age) {
        UserProfile profile = mock(UserProfile.class);
        lenient().when(profile.getAge()).thenReturn(age);
        lenient().when(profile.getNickname()).thenReturn("테스터");
        lenient().when(profile.getMbti()).thenReturn(Mbti.ENFP);
        lenient().when(profile.getLocation()).thenReturn("서울");
        return profile;
    }

    private UserProfile createUserProfileWithLifestyle(DrinkingCapacity drinkingCapacity, boolean isSmoke) {
        UserProfile profile = mock(UserProfile.class);
        lenient().when(profile.getAge()).thenReturn(25);
        lenient().when(profile.getNickname()).thenReturn("테스터");
        lenient().when(profile.getMbti()).thenReturn(Mbti.ENFP);
        lenient().when(profile.getLocation()).thenReturn("서울");
        lenient().when(profile.getDrinkingCapacity()).thenReturn(drinkingCapacity);
        lenient().when(profile.isSmoke()).thenReturn(isSmoke);
        return profile;
    }
}
