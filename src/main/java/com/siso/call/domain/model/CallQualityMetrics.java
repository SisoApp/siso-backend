package com.siso.call.domain.model;

import com.siso.common.domain.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 통화 품질 메트릭 엔티티
 * - WebRTC 통화 품질 측정 데이터 저장
 * - 패킷 손실률, 지터, 비트레이트 등 통화 품질 지표
 */
@Entity
@Table(name = "call_quality_metrics", indexes = {
        @Index(name = "idx_call_id", columnList = "call_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallQualityMetrics extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id", nullable = false)
    private Call call;

    @Column(name = "packet_loss_rate")
    private Integer packetLossRate;  // 패킷 손실률 (%)

    @Column(name = "jitter")
    private Integer jitter;  // 지터 (ms) - 패킷 도착 시간 변동

    @Column(name = "round_trip_time")
    private Integer roundTripTime;  // RTT (ms) - 왕복 지연 시간

    @Column(name = "audio_bitrate")
    private Integer audioBitrate;  // 오디오 비트레이트 (kbps)

    @Column(name = "video_bitrate")
    private Integer videoBitrate;  // 비디오 비트레이트 (kbps) - 화상 통화 시

    @Column(name = "audio_codec")
    private String audioCodec;  // 오디오 코덱 (opus, pcmu 등)

    @Column(name = "video_codec")
    private String videoCodec;  // 비디오 코덱 (vp8, h264 등)

    @Column(name = "connection_quality")
    @Enumerated(EnumType.STRING)
    private ConnectionQuality connectionQuality;  // 연결 품질 (EXCELLENT, GOOD, POOR, BAD)

    @Column(name = "client_type")
    private String clientType;  // 클라이언트 타입 (iOS, Android, Web)

    @Column(name = "network_type")
    private String networkType;  // 네트워크 타입 (WiFi, 4G, 5G 등)

    @Builder
    public CallQualityMetrics(Call call, Integer packetLossRate, Integer jitter, Integer roundTripTime,
                               Integer audioBitrate, Integer videoBitrate, String audioCodec, String videoCodec,
                               ConnectionQuality connectionQuality, String clientType, String networkType) {
        this.call = call;
        this.packetLossRate = packetLossRate;
        this.jitter = jitter;
        this.roundTripTime = roundTripTime;
        this.audioBitrate = audioBitrate;
        this.videoBitrate = videoBitrate;
        this.audioCodec = audioCodec;
        this.videoCodec = videoCodec;
        this.connectionQuality = connectionQuality;
        this.clientType = clientType;
        this.networkType = networkType;
    }

    /**
     * 연결 품질 평가
     * - 패킷 손실률, 지터, RTT 기반으로 품질 결정
     */
    public static ConnectionQuality evaluateQuality(Integer packetLoss, Integer jitter, Integer rtt) {
        // 패킷 손실률 기준
        if (packetLoss != null && packetLoss > 5) {
            return ConnectionQuality.BAD;
        }
        if (packetLoss != null && packetLoss > 2) {
            return ConnectionQuality.POOR;
        }

        // 지터 기준 (ms)
        if (jitter != null && jitter > 100) {
            return ConnectionQuality.POOR;
        }

        // RTT 기준 (ms)
        if (rtt != null && rtt > 300) {
            return ConnectionQuality.POOR;
        }
        if (rtt != null && rtt > 200) {
            return ConnectionQuality.GOOD;
        }

        return ConnectionQuality.EXCELLENT;
    }

    /**
     * 연결 품질 Enum
     */
    public enum ConnectionQuality {
        EXCELLENT("우수"),
        GOOD("양호"),
        POOR("나쁨"),
        BAD("매우 나쁨");

        private final String description;

        ConnectionQuality(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
