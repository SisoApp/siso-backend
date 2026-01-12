package com.siso.matching.domain.model;

import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI 매칭 요청 엔티티
 */
@Entity
@Table(name = "matching_requests", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_request_id", columnList = "request_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingRequest extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "request_id", nullable = false, unique = true)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingStatus status;

    @Column(name = "candidates_count")
    private Integer candidatesCount;

    @Column(name = "matched_count")
    private Integer matchedCount;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Builder
    public MatchingRequest(User user) {
        this.user = user;
        this.requestId = UUID.randomUUID().toString();
        this.status = MatchingStatus.PENDING;
    }

    public void updateStatus(MatchingStatus status) {
        this.status = status;
        if (status == MatchingStatus.COMPLETED || status == MatchingStatus.FAILED) {
            this.processedAt = LocalDateTime.now();
        }
    }

    public void updateResult(int candidatesCount, int matchedCount, int processingTimeMs) {
        this.candidatesCount = candidatesCount;
        this.matchedCount = matchedCount;
        this.processingTimeMs = processingTimeMs;
    }
}
