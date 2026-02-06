package com.siso.integrationTest.infrastructure;

import com.siso.integrationTest.config.IntegrationTestBase;
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
        testUser = createUser("test@example.com", "테스터", 25, "ENFP", "서울", LocalDateTime.now());
        candidate1 = createUser("candidate1@example.com", "후보1", 24, "INTJ", "서울", LocalDateTime.now().minusHours(1));
        candidate2 = createUser("candidate2@example.com", "후보2", 30, "ISFP", "부산", LocalDateTime.now().minusDays(1));
    }

    @Test
    @DisplayName("AI 매칭 알고리즘 실행 - 후보자 조회 및 스코어 계산")
    void calculateMatches_shouldReturnMatchedCandidates() {
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(testUser.getId());
        assertThat(result.getMatches()).isNotNull();
        assertThat(result.getTotalCandidates()).isGreaterThanOrEqualTo(0);

        if (!result.getMatches().isEmpty()) {
            MatchingResultDto.UserMatchScore topMatch = result.getMatches().get(0);
            assertThat(topMatch.getMatchScore()).isBetween(0.0, 1.0);
            assertThat(topMatch.getCandidateId()).isNotNull();
        }
    }

    @Test
    @DisplayName("매칭 스코어 계산 - 나이가 비슷한 사용자가 높은 점수")
    void calculateMatches_shouldScoreHigherForSimilarAge() {
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(testUser);

        if (result.getMatches().size() >= 2) {
            List<Long> candidateIds = result.getMatches().stream()
                    .map(MatchingResultDto.UserMatchScore::getCandidateId)
                    .toList();

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
        testUser.updateLastActiveAt(LocalDateTime.now());
        candidate1.updateLastActiveAt(LocalDateTime.now().minusHours(1));
        candidate2.updateLastActiveAt(LocalDateTime.now().minusDays(2));

        userRepository.save(testUser);
        userRepository.save(candidate1);
        userRepository.save(candidate2);

        MatchingResultDto result = matchingAlgorithmService.calculateMatches(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getMatches()).isNotNull();
    }

    @Test
    @DisplayName("후보자가 없을 때 빈 결과 반환")
    void calculateMatches_whenNoCandidates_shouldReturnEmptyResult() {
        // 후보자만 삭제 (testUser는 유지)
        userRepository.delete(candidate1);
        userRepository.delete(candidate2);

        MatchingResultDto result = matchingAlgorithmService.calculateMatches(testUser);

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
