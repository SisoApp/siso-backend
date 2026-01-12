package com.siso.matching.application.service;

import com.siso.image.domain.model.Image;
import com.siso.matching.application.dto.MatchingResult;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import com.siso.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 매칭 알고리즘 서비스
 * - 6가지 요소로 매칭 스코어 계산
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MatchingAlgorithmService {

    private final UserRepository userRepository;

    /**
     * AI 매칭 알고리즘 실행
     */
    public MatchingResult calculateMatches(User user) {
        long startTime = System.currentTimeMillis();

        log.info("Starting matching algorithm for userId={}", user.getId());

        // 1. 후보자 조회 (차단된 사용자 제외, 온라인인 사용자 우선)
        List<User> candidates = findCandidates(user);
        log.info("Found {} candidates for userId={}", candidates.size(), user.getId());

        if (candidates.isEmpty()) {
            log.warn("No candidates found for userId={}", user.getId());
            return new MatchingResult(user.getId(), List.of(), LocalDateTime.now(), 0);
        }

        // 2. 각 후보별 매칭 스코어 계산
        List<MatchingResult.UserMatchScore> scoredMatches = candidates.stream()
                .map(candidate -> calculateMatchScore(user, candidate))
                .filter(score -> score.getMatchScore() >= 0.3)  // 30% 이상만
                .sorted(Comparator.comparingDouble(
                        MatchingResult.UserMatchScore::getMatchScore).reversed()
                )
                .limit(20)  // 상위 20명
                .toList();

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("Matching completed: userId={}, matched={}/{}, time={}ms",
                user.getId(), scoredMatches.size(), candidates.size(), processingTime);

        return new MatchingResult(
                user.getId(),
                scoredMatches,
                LocalDateTime.now(),
                candidates.size()
        );
    }

    /**
     * 후보자 조회 (기본 필터링)
     */
    private List<User> findCandidates(User user) {
        // 모든 사용자 조회 (자신 제외, 삭제/차단되지 않은 사용자)
        return userRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(user.getId()))
                .filter(candidate -> !candidate.isDeleted())
                .filter(candidate -> !candidate.isBlock())
                .filter(candidate -> candidate.getUserProfile() != null)
                .limit(100)  // 성능을 위해 최대 100명만
                .toList();
    }

    /**
     * 매칭 스코어 계산 (0.0 ~ 1.0)
     */
    private MatchingResult.UserMatchScore calculateMatchScore(User user, User candidate) {
        UserProfile userProfile = user.getUserProfile();
        UserProfile candidateProfile = candidate.getUserProfile();

        // 1. 관심사 유사도 (30%)
        double interestScore = calculateInterestSimilarity(user, candidate);

        // 2. 나이 호환성 (20%)
        double ageScore = calculateAgeCompatibility(userProfile, candidateProfile);

        // 3. MBTI 호환성 (15%)
        double mbtiScore = calculateMbtiCompatibility(
                userProfile.getMbti(),
                candidateProfile.getMbti()
        );

        // 4. 지역 근접성 (15%)
        double locationScore = calculateLocationProximity(
                userProfile.getLocation(),
                candidateProfile.getLocation()
        );

        // 5. 활동성 (10% - 최근 접속)
        double activityScore = calculateActivityScore(candidate.getLastActiveAt());

        // 6. 생활습관 호환성 (10%)
        double lifestyleScore = calculateLifestyleCompatibility(userProfile, candidateProfile);

        // 가중치 적용하여 최종 스코어 계산
        double totalScore = (interestScore * 0.3) +
                (ageScore * 0.2) +
                (mbtiScore * 0.15) +
                (locationScore * 0.15) +
                (activityScore * 0.1) +
                (lifestyleScore * 0.1);

        // 프로필 이미지 URL 가져오기
        String profileImageUrl = candidate.getImages().stream()
                .findFirst()
                .map(Image::getPresignedUrl)
                .orElse(null);

        // 관심사 목록 가져오기
        List<String> interests = candidate.getUserInterests().stream()
                .map(ui -> ui.getInterest().getName())
                .limit(3)
                .toList();

        return new MatchingResult.UserMatchScore(
                candidate.getId(),
                candidateProfile.getNickname(),
                candidateProfile.getAge(),
                candidateProfile.getMbti(),
                interests,
                profileImageUrl,
                Math.round(totalScore * 1000.0) / 1000.0  // 소수점 3자리
        );
    }

    /**
     * 1. 관심사 유사도 계산 (Jaccard Similarity)
     */
    private double calculateInterestSimilarity(User user, User candidate) {
        Set<String> userInterests = user.getUserInterests().stream()
                .map(ui -> ui.getInterest().getName())
                .collect(Collectors.toSet());

        Set<String> candidateInterests = candidate.getUserInterests().stream()
                .map(ui -> ui.getInterest().getName())
                .collect(Collectors.toSet());

        if (userInterests.isEmpty() && candidateInterests.isEmpty()) {
            return 0.5;  // 둘 다 없으면 중립
        }

        // 교집합 크기
        Set<String> intersection = new HashSet<>(userInterests);
        intersection.retainAll(candidateInterests);

        // 합집합 크기
        Set<String> union = new HashSet<>(userInterests);
        union.addAll(candidateInterests);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * 2. 나이 호환성 계산
     */
    private double calculateAgeCompatibility(UserProfile userProfile, UserProfile candidateProfile) {
        int ageDiff = Math.abs(userProfile.getAge() - candidateProfile.getAge());

        // 나이 차이가 0이면 1.0, 10살 이상이면 0.0
        return Math.max(0.0, 1.0 - (ageDiff / 10.0));
    }

    /**
     * 3. MBTI 호환성 계산
     */
    private double calculateMbtiCompatibility(String mbti1, String mbti2) {
        if (mbti1 == null || mbti2 == null) {
            return 0.5;  // MBTI 정보 없으면 중립
        }

        // MBTI 궁합 테이블 (간단한 버전)
        Map<String, List<String>> compatibilityMap = Map.of(
                "ENFP", List.of("INTJ", "INFJ"),
                "INFP", List.of("ENFJ", "ENTJ"),
                "ENFJ", List.of("INFP", "ISFP"),
                "INFJ", List.of("ENFP", "ENTP"),
                "ENTP", List.of("INFJ", "INTJ"),
                "INTP", List.of("ENTJ", "ESTJ"),
                "ENTJ", List.of("INTP", "INFP"),
                "INTJ", List.of("ENFP", "ENTP")
        );

        // 완벽한 궁합이면 1.0
        if (compatibilityMap.getOrDefault(mbti1, List.of()).contains(mbti2)) {
            return 1.0;
        }

        // 같은 MBTI면 0.8
        if (mbti1.equals(mbti2)) {
            return 0.8;
        }

        // 2글자 이상 같으면 0.6
        int sameChars = 0;
        for (int i = 0; i < 4; i++) {
            if (mbti1.charAt(i) == mbti2.charAt(i)) {
                sameChars++;
            }
        }

        return sameChars * 0.15;  // 0.0 ~ 0.6
    }

    /**
     * 4. 지역 근접성 계산
     */
    private double calculateLocationProximity(String location1, String location2) {
        if (location1 == null || location2 == null) {
            return 0.5;  // 위치 정보 없으면 중립
        }

        // 같은 시/도면 1.0
        if (location1.equals(location2)) {
            return 1.0;
        }

        // 간단한 버전: 같은 광역시/도면 0.7, 다르면 0.3
        String region1 = location1.split(" ")[0];
        String region2 = location2.split(" ")[0];

        return region1.equals(region2) ? 0.7 : 0.3;
    }

    /**
     * 5. 활동성 점수 (최근 접속 시간 기반)
     */
    private double calculateActivityScore(LocalDateTime lastActiveAt) {
        if (lastActiveAt == null) {
            return 0.0;
        }

        long hoursAgo = ChronoUnit.HOURS.between(lastActiveAt, LocalDateTime.now());

        // 1시간 이내: 1.0, 24시간 이후: 0.0
        return Math.max(0.0, 1.0 - (hoursAgo / 24.0));
    }

    /**
     * 6. 생활습관 호환성 (음주, 흡연)
     */
    private double calculateLifestyleCompatibility(UserProfile user, UserProfile candidate) {
        double score = 0.0;

        // 음주 호환성 (50%)
        if (user.getDrinkingCapacity() != null && candidate.getDrinkingCapacity() != null) {
            int drinkDiff = Math.abs(
                    user.getDrinkingCapacity().ordinal() -
                            candidate.getDrinkingCapacity().ordinal()
            );
            score += Math.max(0.0, 1.0 - (drinkDiff * 0.25)) * 0.5;
        } else {
            score += 0.25;  // 정보 없으면 중립
        }

        // 흡연 호환성 (50%)
        if (user.getSmoking() != null && candidate.getSmoking() != null) {
            boolean sameSmoking = user.getSmoking().equals(candidate.getSmoking());
            score += sameSmoking ? 0.5 : 0.0;
        } else {
            score += 0.25;  // 정보 없으면 중립
        }

        return score;
    }
}
