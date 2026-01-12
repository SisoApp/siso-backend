package com.siso.matching.domain.repository;

import com.siso.matching.domain.model.MatchingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 매칭 요청 Repository
 */
@Repository
public interface MatchingRequestRepository extends JpaRepository<MatchingRequest, Long> {

    /**
     * requestId로 조회
     */
    Optional<MatchingRequest> findByRequestIdAndUserId(String requestId, Long userId);

    /**
     * 사용자의 매칭 이력 조회 (최신순)
     */
    List<MatchingRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}
