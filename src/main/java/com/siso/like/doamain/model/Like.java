package com.siso.like.doamain.model;

import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "is_liked", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isLiked = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Like(User sender, User receiver, boolean isLiked) {
        this.sender = sender;
        this.receiver = receiver;
        sender.addGivenLike(this);
        receiver.addReceivedLike(this);
        this.isLiked = isLiked;
        this.createdAt = LocalDateTime.now();
    }

    public void updateIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }
}
