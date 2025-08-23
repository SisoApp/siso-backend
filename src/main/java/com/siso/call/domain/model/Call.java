package com.siso.call.domain.model;

import com.siso.callreview.domain.model.CallReview;
import com.siso.image.domain.model.Image;
import com.siso.matching.doamain.model.Matching;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "calls")
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

    @OneToMany(mappedBy = "call", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CallReview> callReviews = new ArrayList<>();

    // 양방향 연관 관계 설정
    public void linkMatching(Matching matching) {
        this.matching = matching;
        matching.linkCall(this);
    }

    public void addCallReview(User evaluator, User target, String comment, int rating, boolean wantsToContinueChat) {
        CallReview callReview = CallReview.builder()
                .call(this) // 현재 Call과 연결
                .evaluator(evaluator)
                .target(target)
                .comment(comment)
                .rating(rating)
                .wantsToContinueChat(wantsToContinueChat)
                .build();

        this.callReviews.add(callReview);
    }

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
        if (callStatus == CallStatus.DENY) {
            this.endTime = LocalDateTime.now();
            this.duration = 0L;
        }
    }

    public void startCall() {
        this.startTime = LocalDateTime.now();
    }

    public void endCall() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.duration = Duration.between(this.startTime, this.endTime).getSeconds();
        }
        this.callStatus = CallStatus.ENDED;
    }
}