package com.siso.matching.doamain.model;

import com.siso.call.domain.model.Call;
import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "matching",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user1_id", "user2_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Matching extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false, foreignKey = @ForeignKey(name = "FK_matching_user1"))
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false, foreignKey = @ForeignKey(name = "FK_matching_user2"))
    private User user2;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_status", nullable = false)
    private MatchingStatus matchingStatus;

    @OneToOne(mappedBy = "matching", cascade = CascadeType.ALL, orphanRemoval = true)
    private Call call;

    // 양방향 연관 관계 설정
    public void linkMatchAsUser1(User user) {
        this.user1 = user;
        user.addMatchAsUser1(this);
    }

    public void linkMatchAsUser2(User user) {
        this.user2 = user;
        user.addMatchAsUser2(this);
    }

    public void linkCall(Call call) {
        this.call = call;
        call.linkMatching(this);
    }

    @Builder
    public Matching(User user1, User user2, MatchingStatus matchingStatus) {
        this.user1 = user1;
        this.user2 = user2;
        user1.addMatchAsUser1(this);
        user2.addMatchAsUser2(this);
        this.matchingStatus = matchingStatus;
    }

    public void updateStatus(MatchingStatus matchingStatus) {
        this.matchingStatus = matchingStatus;
    }
}
