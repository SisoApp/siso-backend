package com.siso.user.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.siso.common.domain.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<UserInterest> userInterests = new ArrayList<>();

    @Column(name = "is_online", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isOnline = false;

    @Column(name = "notification_subscribed", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean notificationSubscribed = false;

    @Column(name = "is_block", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isBlock = false;

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    // 양방향 연관 관계 설정
    public void addInterest(Interest interest) {
        UserInterest userInterest = new UserInterest(this, interest);
        this.userInterests.add(userInterest);
    }

    @Builder
    public User(Provider provider, String phoneNumber, String refreshToken, Boolean isOnline, Boolean isBlock, Boolean isDeleted, LocalDateTime deletedAt) {
        this.provider = provider;
        this.phoneNumber = phoneNumber;
        this.refreshToken = refreshToken;
        this.isOnline = isOnline;
        this.isBlock = isBlock;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
    }

    public void deleteUser() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public void updateIsBlock(Boolean isBlock) {
        this.isBlock = isBlock;
    }
}