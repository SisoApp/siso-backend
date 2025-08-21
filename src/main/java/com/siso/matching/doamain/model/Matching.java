package com.siso.matching.doamain.model;

import com.siso.call.domain.model.Call;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "matching")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Matching {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "matchingStatus", nullable = false)
    private MatchingStatus matchingStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "matching", cascade = CascadeType.ALL, orphanRemoval = true)
    private Call call;

    // 양방향 연관 관계 설정
    public void linkCall(Call call) {
        this.call = call;
    }

    @Builder
    public Matching(User sender, User receiver, MatchingStatus matchingStatus) {
        this.sender = sender;
        this.receiver = receiver;
        sender.addMatchAsUser1(this);
        receiver.addMatchAsUser2(this);
        this.matchingStatus = matchingStatus;
        this.createdAt = LocalDateTime.now();
    }

    public void matchSuccess() {
        this.matchingStatus = MatchingStatus.MATCHED;
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(MatchingStatus matchingStatus) {
        this.matchingStatus = matchingStatus;
    }

    public void callCompleted() {
        if (this.matchingStatus != MatchingStatus.MATCHED) { // 이미 매칭된 경우는 그대로
            this.matchingStatus = MatchingStatus.CALL_COMPLETED;
            this.createdAt = LocalDateTime.now();
        }
    }
}
