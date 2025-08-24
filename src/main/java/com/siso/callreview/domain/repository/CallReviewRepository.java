package com.siso.callreview.domain.repository;

import com.siso.callreview.domain.model.CallReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CallReviewRepository extends JpaRepository<CallReview, Long> {
    Optional<CallReview> findByCallIdAndEvaluatorId(Long callId, Long evaluatorId);

    Optional<CallReview> findByCallIdAndTargetId(Long callId, Long targetId);
}

