package com.siso.callreview.domain.model;

import com.siso.call.domain.model.Call;
import com.siso.chat.domain.model.ChatRoom;
import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.parameters.P;

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
    @JoinColumn(name = "call_id", nullable = false)
    private Call call;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @Builder
    public CallReview(Call call, Integer rating, String comment) {
        this.call = call;
        this.rating = rating;
        this.comment = comment;
    }

    public void updateRating(Integer rating) {
        this.rating = rating;
    }

    public void updateComment(String comment) {
        this.comment = comment;
    }
}