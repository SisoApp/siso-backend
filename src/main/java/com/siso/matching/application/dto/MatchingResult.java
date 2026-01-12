package com.siso.matching.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 매칭 결과 (Redis 캐싱용)
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MatchingResult implements Serializable {
    private Long userId;
    private List<UserMatchScore> matches;
    private LocalDateTime generatedAt;
    private int totalCandidates;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserMatchScore implements Serializable {
        private Long candidateId;
        private String nickname;
        private Integer age;
        private String mbti;
        private List<String> interests;
        private String profileImageUrl;
        private Double matchScore;  // 0.0 ~ 1.0
    }
}
