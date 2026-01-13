package com.siso.call.application;

import com.siso.call.dto.request.CallQualityMetricsRequestDto;
import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallQualityMetrics;
import com.siso.call.domain.repository.CallQualityMetricsRepository;
import com.siso.call.domain.repository.CallRepository;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.siso.common.util.LogUtil.*;

/**
 * 통화 품질 모니터링 서비스
 * - WebRTC 통화 품질 메트릭 수집 및 저장
 * - 통화 품질 통계 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallQualityService {

    private final CallQualityMetricsRepository callQualityMetricsRepository;
    private final CallRepository callRepository;

    /**
     * 통화 품질 메트릭 저장
     * - 클라이언트에서 전송한 WebRTC stats 저장
     */
    @Transactional
    public void saveCallQualityMetrics(CallQualityMetricsRequestDto request) {
        // 통화 존재 확인
        Call call = callRepository.findById(request.getCallId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.CALL_NOT_FOUND));

        // 연결 품질 평가
        CallQualityMetrics.ConnectionQuality quality = CallQualityMetrics.evaluateQuality(
                request.getPacketLossRate(),
                request.getJitter(),
                request.getRoundTripTime()
        );

        // 메트릭 저장
        CallQualityMetrics metrics = CallQualityMetrics.builder()
                .call(call)
                .packetLossRate(request.getPacketLossRate())
                .jitter(request.getJitter())
                .roundTripTime(request.getRoundTripTime())
                .audioBitrate(request.getAudioBitrate())
                .videoBitrate(request.getVideoBitrate())
                .audioCodec(request.getAudioCodec())
                .videoCodec(request.getVideoCodec())
                .connectionQuality(quality)
                .clientType(request.getClientType())
                .networkType(request.getNetworkType())
                .build();

        callQualityMetricsRepository.save(metrics);

        // 구조화된 로깅
        log.info("Call quality metrics saved",
                kvs(
                        "callId", call.getId(),
                        "packetLoss", request.getPacketLossRate(),
                        "jitter", request.getJitter(),
                        "rtt", request.getRoundTripTime(),
                        "quality", quality.name()
                )
        );

        // 품질이 나쁜 경우 경고 로그
        if (quality == CallQualityMetrics.ConnectionQuality.POOR ||
                quality == CallQualityMetrics.ConnectionQuality.BAD) {
            log.warn("Poor call quality detected",
                    kvs(
                            "callId", call.getId(),
                            "quality", quality.name(),
                            "packetLoss", request.getPacketLossRate(),
                            "jitter", request.getJitter(),
                            "rtt", request.getRoundTripTime(),
                            "clientType", request.getClientType(),
                            "networkType", request.getNetworkType()
                    )
            );
        }
    }

    /**
     * 특정 통화의 품질 메트릭 조회
     */
    public List<CallQualityMetrics> getCallQualityMetrics(Long callId) {
        return callQualityMetricsRepository.findByCallIdOrderByCreatedAtDesc(callId);
    }

    /**
     * 최근 품질이 나쁜 통화 조회 (지난 24시간)
     */
    public List<CallQualityMetrics> getPoorQualityCalls() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return callQualityMetricsRepository.findPoorQualityCalls(since);
    }

    /**
     * 기간별 평균 품질 메트릭 조회
     */
    public Object[] getAverageQualityMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        return callQualityMetricsRepository.getAverageQualityMetrics(startDate, endDate);
    }
}
