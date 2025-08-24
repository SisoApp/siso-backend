package com.siso.like.doamain.model;

import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseTime {
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
    @Column(name = "like_status", nullable = false)
    private LikeStatus likeStatus = LikeStatus.ACTIVE;

    @Builder
    public Like(User sender, User receiver, LikeStatus likeStatus) {
        this.sender = sender;
        this.receiver = receiver;
        sender.addGivenLike(this);
        receiver.addReceivedLike(this);
        this.likeStatus = likeStatus;
    }

    // 양방향 연관 관계 설정
    public void linkGivenLike(User user) {
        this.sender = user;
        user.addGivenLike(this);
    }

    public void linkReceivedLike(User user) {
        this.receiver = user;
        user.addReceivedLike(this);
    }

    public void updateLikeStatus(LikeStatus likeStatus) {
        this.likeStatus = likeStatus;
    }

    public void cancel() {
        this.likeStatus = LikeStatus.CANCELED;
    }
}
