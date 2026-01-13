package com.siso.call.presentation;

import com.siso.call.dto.request.CallQualityMetricsRequestDto;
import com.siso.call.application.CallQualityService;
import com.siso.call.domain.model.CallQualityMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 통화 품질 모니터링 API
 * - WebRTC 통화 품질 메트릭 수집 및 조회
 */
@Tag(name = "Call Quality", description = "통화 품질 모니터링 API")
@Slf4j
@RestController
@RequestMapping("/api/call-quality")
@RequiredArgsConstructor
public class CallQualityController {

    private final CallQualityService callQualityService;

    /**
     * 통화 품질 메트릭 제출
     * - 클라이언트(iOS/Android)에서 WebRTC stats를 수집하여 전송
     */
    @Operation(summary = "통화 품질 메트릭 제출", description = "클라이언트에서 수집한 WebRTC 통화 품질 데이터 제출")
    @PostMapping("/metrics")
    public ResponseEntity<Void> submitCallQualityMetrics(
            @Valid @RequestBody CallQualityMetricsRequestDto request
    ) {
        log.info("Received call quality metrics: callId={}", request.getCallId());
        callQualityService.saveCallQualityMetrics(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 통화의 품질 메트릭 조회
     */
    @Operation(summary = "통화 품질 조회", description = "특정 통화의 품질 메트릭 목록 조회")
    @GetMapping("/metrics/{callId}")
    public ResponseEntity<List<CallQualityMetrics>> getCallQualityMetrics(
            @PathVariable Long callId
    ) {
        List<CallQualityMetrics> metrics = callQualityService.getCallQualityMetrics(callId);
        return ResponseEntity.ok(metrics);
    }

    /**
     * 품질이 나쁜 통화 조회 (지난 24시간)
     */
    @Operation(summary = "품질 나쁜 통화 조회", description = "최근 24시간 내 품질이 나쁜 통화 목록")
    @GetMapping("/poor-quality")
    public ResponseEntity<List<CallQualityMetrics>> getPoorQualityCalls() {
        List<CallQualityMetrics> poorCalls = callQualityService.getPoorQualityCalls();
        return ResponseEntity.ok(poorCalls);
    }

    /**
     * 기간별 평균 품질 통계
     */
    @Operation(summary = "평균 품질 통계", description = "지정 기간의 평균 통화 품질 통계")
    @GetMapping("/average")
    public ResponseEntity<Object[]> getAverageQualityMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        Object[] averageMetrics = callQualityService.getAverageQualityMetrics(startDate, endDate);
        return ResponseEntity.ok(averageMetrics);
    }
}
