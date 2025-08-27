package com.siso.call.domain.model;

import com.siso.callreview.domain.model.CallReview;
import com.siso.chat.domain.model.ChatRoom;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false, foreignKey = @ForeignKey(name = "FK_likes_sender"))
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false, foreignKey = @ForeignKey(name = "FK_likes_receiver"))
    private User receiver;

    @OneToOne(mappedBy = "call", cascade = CascadeType.ALL, orphanRemoval = true)
    private ChatRoom chatRoom;

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
    public void linkCaller(User user) {
        this.caller = user;
        user.addCaller(this);
    }

    public void linkReceiver(User user) {
        this.receiver = user;
        user.addReceiver(this);
    }

    public void linkChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        chatRoom.linkCall(this);
    }

    public void addCallReview(String comment, int rating) {
        CallReview callReview = CallReview.builder()
                .call(this) // 현재 Call과 연결
                .comment(comment)
                .rating(rating)
                .build();

        this.callReviews.add(callReview);
    }

    @Builder
    public Call(User caller, User receiver, CallStatus callStatus, LocalDateTime startTime, LocalDateTime endTime, Long duration, String agoraChannelName, String agoraToken) {
        this.caller = caller;
        this.receiver = receiver;
        caller.addCaller(this);
        receiver.addReceiver(this);
        this.callStatus = callStatus;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.agoraChannelName = agoraChannelName;
        this.agoraToken = agoraToken;
    }

    public void updateCallStatus(CallStatus callStatus) {
        this.callStatus = callStatus;
    }

    // 시작
    public void startCall() {
        this.startTime = LocalDateTime.now();
        this.endTime = null;
        this.duration = 0L;
    }

    // 종료
    public void endCall() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.duration = Duration.between(this.startTime, this.endTime).getSeconds();
        }
        this.callStatus = CallStatus.ENDED;
    }

    // 종료 (첫 통화 제한 여부 적용)
    public void endCall(boolean isFirstCallLimited) {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.duration = Duration.between(this.startTime, this.endTime).getSeconds();

            // 최초 통화만 8분 제한 적용
            if (isFirstCallLimited && this.duration > 480) {
                this.duration = 480L;
            }
        }
        this.callStatus = CallStatus.ENDED;
    }
}