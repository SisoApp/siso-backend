package com.siso.callreview.domain.model;

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
        name = "call_reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"evaluator", "target"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallReview extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator", nullable = false, foreignKey = @ForeignKey(name = "FK_call_reviews_evaluator"))
    private User evaluator;  // 평가자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target",nullable = false, foreignKey = @ForeignKey(name = "FK_call_reviews_target"))
    private User target;     // 평가 대상

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id", nullable = false)
    private Call call;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "wants_to_continue_chat")
    private Boolean wantsToContinueChat;

    // 양방향 연관 관계 설정
    public void linkEvaluator(User user) {
        this.evaluator = user;
        user.addEvaluator(this);
    }

    public void linkTarget(User user) {
        this.target = user;
        user.addTarget(this);
    }

    @Builder
    public CallReview(Call call, User evaluator, User target, Integer rating, String comment, Boolean wantsToContinueChat) {
        this.call = call;
        this.evaluator = evaluator;
        this.target = target;
        this.rating = rating;
        this.comment = comment;
        this.wantsToContinueChat = wantsToContinueChat;
    }
}
