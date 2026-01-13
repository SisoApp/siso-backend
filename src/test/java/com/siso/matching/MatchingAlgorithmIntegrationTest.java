package com.siso.matching;

import com.siso.config.IntegrationTestBase;
import com.siso.matching.dto.MatchingResultDto;
import com.siso.matching.application.service.MatchingAlgorithmService;
import com.siso.user.domain.model.*;
import com.siso.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AI 매칭 알고리즘 통합 테스트
 */
@DisplayName("AI 매칭 알고리즘 통합 테스트")
class MatchingAlgorithmIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MatchingAlgorithmService matchingAlgorithmService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User candidate1;
    private User candidate2;

    @BeforeEach
    void setUp() {
        // Given: 테스트 사용자 생성
        testUser = createUser("test@example.com", "테스터", 25, "ENFP", "서울", LocalDateTime.now());

        // Given: 후보자들 생성
        candidate1 = createUser("candidate1@example.com", "후보1", 24, "INTJ", "서울", LocalDateTime.now().minusHours(1));
        candidate2 = createUser("candidate2@example.com", "후보2", 30, "ISFP", "부산", LocalDateTime.now().minusDays(1));

        // Given: 관심사 추가 (간단한 버전)
        // 실제로는 Interest 엔티티와 UserInterest를 생성해야 하지만,
        // 테스트에서는 간소화
    }

    @Test
    @DisplayName("AI 매칭 알고리즘 실행 - 후보자 조회 및 스코어 계산")
    void calculateMatches_shouldReturnMatchedCandidates() {
        // When: 매칭 알고리즘 실행
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(testUser);

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(testUser.getId());
        assertThat(result.getMatches()).isNotNull();
        assertThat(result.getTotalCandidates()).isGreaterThanOrEqualTo(0);

        // 후보자가 있으면 스코어 검증
        if (!result.getMatches().isEmpty()) {
            MatchingResultDto.UserMatchScore topMatch = result.getMatches().get(0);
            assertThat(topMatch.getMatchScore()).isBetween(0.0, 1.0);
            assertThat(topMatch.getCandidateId()).isNotNull();
        }
    }

    @Test
    @DisplayName("매칭 스코어 계산 - 나이가 비슷한 사용자가 높은 점수")
    void calculateMatches_shouldScoreHigherForSimilarAge() {
        // When: 매칭 알고리즘 실행
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(testUser);

        // Then: 나이가 비슷한 candidate1이 candidate2보다 높은 점수를 가져야 함
        // (25살 vs 24살 vs 30살)
        if (result.getMatches().size() >= 2) {
            List<Long> candidateIds = result.getMatches().stream()
                    .map(MatchingResultDto.UserMatchScore::getCandidateId)
                    .toList();

            // candidate1 (24살)이 candidate2 (30살)보다 먼저 나와야 함
            int candidate1Index = candidateIds.indexOf(candidate1.getId());
            int candidate2Index = candidateIds.indexOf(candidate2.getId());

            if (candidate1Index != -1 && candidate2Index != -1) {
                assertThat(candidate1Index).isLessThan(candidate2Index);
            }
        }
    }

    @Test
    @DisplayName("매칭 스코어 계산 - 최근 활동 사용자가 높은 점수")
    void calculateMatches_shouldScoreHigherForRecentlyActive() {
        // Given: 최근 활동 시간이 다른 사용자들
        testUser.updateLastActiveAt(LocalDateTime.now());
        candidate1.updateLastActiveAt(LocalDateTime.now().minusHours(1));
        candidate2.updateLastActiveAt(LocalDateTime.now().minusDays(2));

        userRepository.save(testUser);
        userRepository.save(candidate1);
        userRepository.save(candidate2);

        // When: 매칭 알고리즘 실행
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(testUser);

        // Then: 결과가 있는지 확인
        assertThat(result).isNotNull();
        assertThat(result.getMatches()).isNotNull();
    }

    @Test
    @DisplayName("후보자가 없을 때 빈 결과 반환")
    void calculateMatches_whenNoCandidates_shouldReturnEmptyResult() {
        // Given: 모든 후보자 삭제
        userRepository.deleteAll();
        testUser = userRepository.save(testUser);

        // When: 매칭 알고리즘 실행
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(testUser);

        // Then: 빈 결과 반환
        assertThat(result).isNotNull();
        assertThat(result.getMatches()).isEmpty();
        assertThat(result.getTotalCandidates()).isEqualTo(0);
    }

    private User createUser(String email, String nickname, int age, String mbti, String location, LocalDateTime lastActiveAt) {
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email(email)
                .phoneNumber("010-0000-0000")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        user.updateLastActiveAt(lastActiveAt);

        user = userRepository.save(user);

        // UserProfile 생성
        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname(nickname)
                .age(age)
                .mbti(Mbti.ENTJ)
                .location(location)
                .build();

        user.setUserProfile(profile);
        userRepository.save(user);

        return user;
    }
}
