package com.siso.user.infrastructure.persistence;

import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.User;

import java.util.List;

/**
 * User Custom Repository
 * - QueryDSL을 사용한 동적 쿼리
 * - N+1 쿼리 문제 해결을 위한 Fetch Join
 */
public interface UserRepositoryCustom {

    /**
     * 여러 ID로 사용자 조회 (연관 엔티티 함께 조회)
     * - N+1 문제 해결: images, userProfile, voiceSample fetch join
     */
    List<User> findByIdsWithAllRelations(List<Long> ids);

    /**
     * 동적 필터로 사용자 검색
     * - 성별, 나이, 온라인 상태 등
     */
    List<User> findUsersWithDynamicFilters(String gender, Integer minAge, Integer maxAge,
                                            PresenceStatus presenceStatus);

    /**
     * 매칭 후보 조회 (연관 엔티티 포함)
     * - AI 매칭 알고리즘에 사용
     * - images, userProfile, userInterests 함께 조회
     */
    List<User> findMatchingCandidatesWithRelations(Long excludeUserId, List<String> preferredGenders,
                                                    Integer minAge, Integer maxAge);
}
