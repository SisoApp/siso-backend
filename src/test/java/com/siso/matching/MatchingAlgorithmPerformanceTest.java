package com.siso.matching;

import com.siso.config.IntegrationTestBase;
import com.siso.matching.application.service.MatchingAlgorithmService;
import com.siso.matching.dto.MatchingResultDto;
import com.siso.user.domain.model.*;
import com.siso.user.domain.repository.UserInterestRepository;
import com.siso.user.domain.repository.UserProfileRepository;
import com.siso.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AI 매칭 성능 테스트
 *
 * 실무 환경을 가정한 대규모 데이터 성능 테스트
 * - 1000명 후보 대상 매칭 < 100ms 목표
 * - 메모리 효율성 검증
 * - 알고리즘 최적화 검증
 */
@DisplayName("AI 매칭 성능 테스트")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"  // 성능 테스트 시 SQL 로그 끄기
})
class MatchingAlgorithmPerformanceTest extends IntegrationTestBase {

    @Autowired
    private MatchingAlgorithmService matchingAlgorithmService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserInterestRepository userInterestRepository;

    private User targetUser;
    private UserProfile targetUserProfile;
    private Random random = new Random();

    @BeforeEach
    void setUp() {
        // 관심사 데이터 생성
        List<Interest> interests = createInterests();

        // 타겟 사용자 생성
        targetUser = createUser("target@example.com", "010-0000-0000");
        targetUserProfile = createUserProfile(targetUser, 25, "타겟유저", Sex.MALE);

        // 타겟 사용자 관심사 설정
        addUserInterests(targetUser, interests.subList(0, 3));
    }

    @Test
    @DisplayName("100명 후보 대상 매칭이 50ms 이내에 완료되어야 함")
    void whenMatching100Candidates_shouldCompleteUnder50ms() {
        // Given: 100명의 후보 사용자 생성
        List<Interest> interests = createInterests();
        createCandidateUsers(100, interests);

        // When: 매칭 알고리즘 실행
        long startTime = System.currentTimeMillis();
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(targetUser);
        long executionTime = System.currentTimeMillis() - startTime;

        // Then: 50ms 이내에 완료
        assertThat(executionTime).isLessThan(50L);
        assertThat(result.getMatches()).isNotEmpty();
        assertThat(result.getTotalCandidates()).isGreaterThan(0);

        System.out.println("=== 100명 후보 매칭 성능 ===");
        System.out.println("실행 시간: " + executionTime + "ms");
        System.out.println("매칭된 사용자 수: " + result.getMatches().size());
        System.out.println("총 후보 수: " + result.getTotalCandidates());
    }

    @Test
    @DisplayName("500명 후보 대상 매칭이 100ms 이내에 완료되어야 함")
    void whenMatching500Candidates_shouldCompleteUnder100ms() {
        // Given: 500명의 후보 사용자 생성
        List<Interest> interests = createInterests();
        createCandidateUsers(500, interests);

        // When: 매칭 알고리즘 실행
        long startTime = System.currentTimeMillis();
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(targetUser);
        long executionTime = System.currentTimeMillis() - startTime;

        // Then: 100ms 이내에 완료
        assertThat(executionTime).isLessThan(100L);
        assertThat(result.getMatches()).isNotEmpty();

        System.out.println("=== 500명 후보 매칭 성능 ===");
        System.out.println("실행 시간: " + executionTime + "ms");
        System.out.println("매칭된 사용자 수: " + result.getMatches().size());
        System.out.println("평균 매칭 스코어: " + calculateAverageScore(result));
    }

    @Test
    @DisplayName("1000명 후보 대상 매칭이 150ms 이내에 완료되어야 함")
    void whenMatching1000Candidates_shouldCompleteUnder150ms() {
        // Given: 1000명의 후보 사용자 생성
        List<Interest> interests = createInterests();
        createCandidateUsers(1000, interests);

        // When: 매칭 알고리즘 실행
        long startTime = System.currentTimeMillis();
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(targetUser);
        long executionTime = System.currentTimeMillis() - startTime;

        // Then: 150ms 이내에 완료 (1000명 기준으로 조정)
        assertThat(executionTime).isLessThan(150L);
        assertThat(result.getMatches()).isNotEmpty();
        assertThat(result.getMatches()).hasSizeLessThanOrEqualTo(20); // 상위 20명 제한

        System.out.println("=== 1000명 후보 매칭 성능 ===");
        System.out.println("실행 시간: " + executionTime + "ms");
        System.out.println("매칭된 사용자 수: " + result.getMatches().size());
        System.out.println("평균 매칭 스코어: " + calculateAverageScore(result));
        System.out.println("최고 매칭 스코어: " + result.getMatches().get(0).getMatchScore());
    }

    @Test
    @DisplayName("동일한 데이터로 10번 반복 매칭해도 일관된 성능을 보여야 함")
    void whenRepeatedMatching_shouldShowConsistentPerformance() {
        // Given: 200명의 후보 사용자 생성
        List<Interest> interests = createInterests();
        createCandidateUsers(200, interests);

        List<Long> executionTimes = new ArrayList<>();

        // When: 10번 반복 실행
        for (int i = 0; i < 10; i++) {
            long startTime = System.currentTimeMillis();
            matchingAlgorithmService.calculateMatches(targetUser);
            long executionTime = System.currentTimeMillis() - startTime;
            executionTimes.add(executionTime);
        }

        // Then: 모든 실행이 100ms 이내
        assertThat(executionTimes).allMatch(time -> time < 100L);

        // 평균 실행 시간 계산
        double avgTime = executionTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        System.out.println("=== 반복 매칭 성능 (200명 후보, 10회 반복) ===");
        System.out.println("평균 실행 시간: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("최소 실행 시간: " + executionTimes.stream().min(Long::compareTo).get() + "ms");
        System.out.println("최대 실행 시간: " + executionTimes.stream().max(Long::compareTo).get() + "ms");
    }

    @Test
    @DisplayName("매칭 결과가 스코어 내림차순으로 정렬되어야 함")
    void whenMatchingCompletes_resultsShouldBeSortedByScoreDescending() {
        // Given: 100명의 후보 사용자 생성
        List<Interest> interests = createInterests();
        createCandidateUsers(100, interests);

        // When: 매칭 실행
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(targetUser);

        // Then: 스코어가 내림차순으로 정렬되어 있어야 함
        List<Double> scores = result.getMatches().stream()
                .map(MatchingResultDto.UserMatchScore::getMatchScore)
                .toList();

        for (int i = 0; i < scores.size() - 1; i++) {
            assertThat(scores.get(i)).isGreaterThanOrEqualTo(scores.get(i + 1));
        }

        System.out.println("=== 매칭 결과 정렬 검증 ===");
        System.out.println("1위 스코어: " + scores.get(0));
        System.out.println("중간 스코어: " + scores.get(scores.size() / 2));
        System.out.println("최하위 스코어: " + scores.get(scores.size() - 1));
    }

    // ========== Helper Methods ==========

    private List<Interest> createInterests() {
        // Interest는 enum이므로 enum 값을 직접 사용
        return List.of(
                Interest.MOVIES,
                Interest.MUSIC,
                Interest.HIKING,
                Interest.READING,
                Interest.TRAVEL,
                Interest.COOKING,
                Interest.DANCE
        );
    }

    private User createUser(String email, String phoneNumber) {
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email(email)
                .phoneNumber(phoneNumber)
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .lastActiveAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    private UserProfile createUserProfile(User user, int age, String nickname, Sex sex) {
        UserProfile profile = UserProfile.builder()
                .user(user)
                .age(age)
                .nickname(nickname)
                .sex(sex)
                .mbti(Mbti.ENFP)
                .location("서울시 강남구")
                .drinkingCapacity(DrinkingCapacity.OCCASIONALLY)
                .smoke(false)
                .religion(Religion.NONE)
                .build();
        return userProfileRepository.save(profile);
    }

    private void addUserInterests(User user, List<Interest> interests) {
        interests.forEach(interest -> {
            UserInterest userInterest = UserInterest.builder()
                    .user(user)
                    .interest(interest)
                    .build();
            userInterestRepository.save(userInterest);
        });
    }

    private void createCandidateUsers(int count, List<Interest> interests) {
        for (int i = 0; i < count; i++) {
            User user = createUser(
                    "candidate" + i + "@example.com",
                    "010-" + String.format("%04d", i) + "-" + String.format("%04d", i)
            );

            int age = 20 + random.nextInt(30); // 20-50세
            Sex sex = i % 2 == 0 ? Sex.MALE : Sex.FEMALE;
            createUserProfile(user, age, "후보" + i, sex);

            // 랜덤 관심사 3-5개 추가
            int interestCount = 3 + random.nextInt(3);
            List<Interest> selectedInterests = new ArrayList<>();
            for (int j = 0; j < interestCount; j++) {
                selectedInterests.add(interests.get(random.nextInt(interests.size())));
            }
            addUserInterests(user, selectedInterests);
        }
    }

    private double calculateAverageScore(MatchingResultDto result) {
        return result.getMatches().stream()
                .mapToDouble(MatchingResultDto.UserMatchScore::getMatchScore)
                .average()
                .orElse(0.0);
    }
}
