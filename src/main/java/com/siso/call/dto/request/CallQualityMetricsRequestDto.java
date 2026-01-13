package com.siso.call.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 통화 품질 메트릭 제출 요청 DTO
 * - 클라이언트(iOS/Android)에서 WebRTC stats를 수집하여 전송
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallQualityMetricsRequestDto {

    @NotNull(message = "Call ID는 필수입니다")
    private Long callId;

    @Min(0) @Max(100)
    private Integer packetLossRate;  // 패킷 손실률 (0-100%)

    @Min(0)
    private Integer jitter;  // 지터 (ms)

    @Min(0)
    private Integer roundTripTime;  // RTT (ms)

    @Min(0)
    private Integer audioBitrate;  // 오디오 비트레이트 (kbps)

    @Min(0)
    private Integer videoBitrate;  // 비디오 비트레이트 (kbps)

    private String audioCodec;  // 오디오 코덱

    private String videoCodec;  // 비디오 코덱

    private String clientType;  // iOS, Android, Web

    private String networkType;  // WiFi, 4G, 5G 등
}
