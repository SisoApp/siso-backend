package com.siso.user.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest", nullable = false)
    @JsonBackReference
    private Interest interest;

    @Builder
    public UserInterest(User user, Interest interest) {
        this.user = user;
        this.interest = interest;
    }
}
