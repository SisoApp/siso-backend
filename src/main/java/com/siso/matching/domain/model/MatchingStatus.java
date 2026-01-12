package com.siso.matching.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 매칭 요청 상태
 */
@Getter
@RequiredArgsConstructor
public enum MatchingStatus {
    PENDING("매칭 대기"),
    PROCESSING("매칭 처리 중"),
    COMPLETED("매칭 완료"),
    FAILED("매칭 실패");

    private final String description;
}
