package com.siso.user.domain.model;

import com.siso.common.domain.BaseTime;
import com.siso.image.domain.model.Image;
import com.siso.like.doamain.model.Like;
import com.siso.matching.doamain.model.Matching;
import com.siso.voicesample.domain.model.VoiceSample;
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

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number" , nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "presence_status", nullable = false)
    private PresenceStatus presenceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false)
    private RegistrationStatus registrationStatus;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "notification_subscribed", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean notificationSubscribed = false;

    @Column(name = "is_block", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isBlock = false;

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private VoiceSample voiceSample;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserInterest> userInterests = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> givenLikes = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> receivedLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matching> matchAsUser1 = new ArrayList<>();

    @OneToMany(mappedBy = "user2", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matching> matchAsUser2 = new ArrayList<>();

    // 양방향 연관 관계 설정
    public void linkProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        userProfile.linkUser(this);
    }

    public void linkVoiceSample(VoiceSample voiceSample) {
        this.voiceSample = voiceSample;
        voiceSample.linkUser(this);
    }

    public void addInterest(Interest interest) {
        UserInterest userInterest = UserInterest.builder()
                .user(this)
                .interest(interest)
                .build();
        this.userInterests.add(userInterest);
    }

    public void addImage(String path, String serverImageName, String originalName) {
        Image image = Image.builder()
                .user(this)
                .path(path)
                .serverImageName(serverImageName)
                .originalName(originalName)
                .build();
        this.images.add(image);
    }

    public void addGivenLike(Like like) {
        this.givenLikes.add(like);
        like.linkGivenLike(this);
    }

    public void addReceivedLike(Like like) {
        this.receivedLikes.add(like);
        like.linkReceivedLike(this);
    }

    public void addMatchAsUser1(Matching matching) {
        this.matchAsUser1.add(matching);
        matching.linkMatchAsUser1(this);
    }

    public void addMatchAsUser2(Matching matching) {
        this.matchAsUser2.add(matching);
        matching.linkMatchAsUser2(this);
    }

    @Builder
    public User(Provider provider, String email, String phoneNumber, PresenceStatus presenceStatus, RegistrationStatus registrationStatus, String refreshToken, boolean notificationSubscribed, boolean isBlock, boolean isDeleted, LocalDateTime deletedAt, LocalDateTime lastActiveAt) {
        this.provider = provider;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.presenceStatus = presenceStatus;
        this.registrationStatus = registrationStatus;
        this.refreshToken = refreshToken;
        this.notificationSubscribed = notificationSubscribed;
        this.isBlock = isBlock;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
        this.lastActiveAt = lastActiveAt;
    }

    @Builder
    public User(Provider provider, String phoneNumber) {
        this.provider = provider;
        this.phoneNumber = phoneNumber;
    }

    // 기존 관심사를 모두 삭제하고 새로운 관심사로 갱신
    public void updateUserInterests(List<Interest> interests) {
        this.userInterests.clear();
        for (Interest interest : interests) {
            this.addInterest(interest);
        }
    }

    public void deleteUser() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateNotificationSubScribed(boolean notificationSubscribed) {
        this.notificationSubscribed = notificationSubscribed;
    }

    public void updatePresenceStatus(PresenceStatus presenceStatus) {
        this.presenceStatus = presenceStatus;
    }

    public void updateRegistrationStatus(RegistrationStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public void updateIsBlock(boolean isBlock) {
        this.isBlock = isBlock;
    }
}