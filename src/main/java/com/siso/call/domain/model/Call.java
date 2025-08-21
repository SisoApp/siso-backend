package com.siso.call.domain.model;

import com.siso.matching.doamain.model.Matching;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "Calls")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Call {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus callStatus;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "agora_channel_name", nullable = false)
    private String agoraChannelName;

    @Column(name = "agora_token", nullable = false)
    private String agoraToken;

    @Builder
    public Call(Matching matching, CallStatus callStatus, LocalDateTime startTime, LocalDateTime endTime, Long duration, String agoraChannelName, String agoraToken) {
        this.matching = matching;
        matching.linkCall(this);
        this.callStatus = callStatus;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.agoraChannelName = agoraChannelName;
        this.agoraToken = agoraToken;
    }

    public void updateCallStatus(CallStatus callStatus) {
        this.callStatus = callStatus;
        if (callStatus == CallStatus.Deny) {
            this.endTime = LocalDateTime.now();
            this.duration = 0L;
        }
    }

    public void startCall(String channelName, String token) {
        this.agoraChannelName = channelName;
        this.agoraToken = token;
        this.startTime = LocalDateTime.now();
        this.callStatus = null; // 수신 전 상태
    }

    public void endCall() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.duration = Duration.between(this.startTime, this.endTime).getSeconds();
        }
        this.callStatus = CallStatus.Accept;
    }
}
