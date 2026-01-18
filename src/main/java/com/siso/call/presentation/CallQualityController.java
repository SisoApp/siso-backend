package com.siso.call.presentation;

import com.siso.call.dto.request.CallQualityMetricsRequestDto;
import com.siso.call.application.CallQualityService;
import com.siso.call.domain.model.CallQualityMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
public class CallQualityController {

    private final CallQualityService callQualityService;

    @Operation(
            summary = "통화 품질 메트릭 제출",
            description = "클라이언트에서 수집한 WebRTC 통화 품질 데이터를 제출합니다. " +
                    "패킷 손실률, 지터, RTT, 비트레이트 등의 메트릭을 수집하여 통화 품질을 자동 평가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제출 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 또는 범위 오류)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "통화 정보 없음", content = @Content)
    })
    @PostMapping("/metrics")
    public ResponseEntity<Void> submitCallQualityMetrics(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "통화 품질 메트릭 데이터 (패킷 손실률, 지터, RTT, 비트레이트 등)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CallQualityMetricsRequestDto.class))
            )
            @Valid @RequestBody CallQualityMetricsRequestDto request
    ) {
        log.info("Received call quality metrics: callId={}", request.getCallId());
        callQualityService.saveCallQualityMetrics(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "통화 품질 조회",
            description = "특정 통화의 품질 메트릭 목록을 조회합니다. 시간순으로 정렬되어 반환됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "통화 정보 없음", content = @Content)
    })
    @GetMapping("/metrics/{callId}")
    public ResponseEntity<List<CallQualityMetrics>> getCallQualityMetrics(
            @Parameter(description = "통화 ID", required = true, example = "1")
            @PathVariable Long callId
    ) {
        List<CallQualityMetrics> metrics = callQualityService.getCallQualityMetrics(callId);
        return ResponseEntity.ok(metrics);
    }

    @Operation(
            summary = "품질 나쁜 통화 조회",
            description = "최근 24시간 내 품질이 나쁜(POOR/BAD) 통화 목록을 조회합니다. " +
                    "문제 통화를 모니터링하여 서비스 품질을 개선하는데 활용할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @GetMapping("/poor-quality")
    public ResponseEntity<List<CallQualityMetrics>> getPoorQualityCalls() {
        List<CallQualityMetrics> poorCalls = callQualityService.getPoorQualityCalls();
        return ResponseEntity.ok(poorCalls);
    }

    @Operation(
            summary = "평균 품질 통계",
            description = "지정한 기간의 평균 통화 품질 통계를 조회합니다. " +
                    "평균 패킷 손실률, 지터, RTT, 비트레이트 등을 확인할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @GetMapping("/average")
    public ResponseEntity<Object[]> getAverageQualityMetrics(
            @Parameter(
                    description = "시작 날짜 (ISO 8601 형식)",
                    required = true,
                    example = "2025-01-01T00:00:00"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(
                    description = "종료 날짜 (ISO 8601 형식)",
                    required = true,
                    example = "2025-01-18T23:59:59"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        Object[] averageMetrics = callQualityService.getAverageQualityMetrics(startDate, endDate);
        return ResponseEntity.ok(averageMetrics);
    }
}
