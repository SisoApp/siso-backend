package com.siso.matching.doamain.model;

import com.siso.user.domain.model.User;
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
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Matching(User receiver, User sender, Status status, LocalDateTime createdAt) {
        this.receiver = receiver;
        this.sender = sender;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void matchSuccess() {
        this.status = Status.MATCHED;
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void callCompleted() {
        if (this.status != Status.MATCHED) { // 이미 매칭된 경우는 그대로
            this.status = Status.CALL_COMPLETED;
            this.createdAt = LocalDateTime.now();
        }
    }
}
