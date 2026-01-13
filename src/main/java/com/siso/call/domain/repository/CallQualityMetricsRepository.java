package com.siso.call.domain.repository;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallQualityMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 통화 품질 메트릭 Repository
 */
public interface CallQualityMetricsRepository extends JpaRepository<CallQualityMetrics, Long> {

    /**
     * 특정 통화의 품질 메트릭 조회
     */
    List<CallQualityMetrics> findByCallOrderByCreatedAtDesc(Call call);

    /**
     * 특정 통화의 품질 메트릭 조회 (Call ID로)
     */
    @Query("SELECT cqm FROM CallQualityMetrics cqm WHERE cqm.call.id = :callId ORDER BY cqm.createdAt DESC")
    List<CallQualityMetrics> findByCallIdOrderByCreatedAtDesc(@Param("callId") Long callId);

    /**
     * 기간별 통화 품질 통계
     */
    @Query("""
        SELECT AVG(cqm.packetLossRate), AVG(cqm.jitter), AVG(cqm.roundTripTime)
        FROM CallQualityMetrics cqm
        WHERE cqm.createdAt BETWEEN :startDate AND :endDate
    """)
    Object[] getAverageQualityMetrics(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * 품질이 나쁜 통화 조회
     */
    @Query("""
        SELECT cqm FROM CallQualityMetrics cqm
        WHERE cqm.connectionQuality IN ('POOR', 'BAD')
        AND cqm.createdAt >= :since
        ORDER BY cqm.createdAt DESC
    """)
    List<CallQualityMetrics> findPoorQualityCalls(@Param("since") LocalDateTime since);
}
