package com.siso.user.domain.model;

import com.siso.call.domain.model.Call;
import com.siso.chat.domain.model.ChatMessage;
import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.domain.model.ChatRoomLimit;
import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.common.domain.BaseTime;
import com.siso.image.domain.model.Image;
import com.siso.report.domain.model.Report;
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

    // 일대일 관계
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private VoiceSample voiceSample;

    // 일대다 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserInterest> userInterests = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomMember> chatRoomMembers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomLimit> chatRoomLimits = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    // 다대다 관계
    @OneToMany(mappedBy = "caller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Call> caller = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Call> receiver = new ArrayList<>();

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reporter = new ArrayList<>();

    @OneToMany(mappedBy = "reported", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reported = new ArrayList<>();

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

    public void addChatRoomMember(ChatRoom chatRoom, Long lastReadMessageId) {
        ChatRoomMember chatRoomMember = ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .user(this)
                .lastReadMessageId(lastReadMessageId)
                .build();
        this.chatRoomMembers.add(chatRoomMember);
    }

    public ChatRoomLimit addChatRoomLimit(ChatRoom chatRoom, int messageCount) {
        ChatRoomLimit chatRoomLimit = ChatRoomLimit.builder()
                .chatRoom(chatRoom)
                .user(this)
                .messageCount(messageCount)
                .build();
        this.chatRoomLimits.add(chatRoomLimit);
        return chatRoomLimit;
    }

    public void addChatMessage(ChatRoom chatRoom,String content) {
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(this)
                .chatRoom(chatRoom)
                .content(content)
                .build();
        this.chatMessages.add(chatMessage);
    }

    public void addCaller(Call call) {
        this.caller.add(call);
        call.linkCaller(this);
    }

    public void addReceiver(Call call) {
        this.receiver.add(call);
        call.linkReceiver(this);
    }

    public void addReporter(Report report) {
        this.reporter.add(report);
        report.linkReporter(this);
    }

    public void addReported(Report report) {
        this.reported.add(report);
        report.linkReported(this);
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

    public void reActivateUser() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    // 30일이 지났는지 체크
    public boolean isEligibleForHardDelete() {
        return isDeleted && deletedAt != null && deletedAt.isBefore(LocalDateTime.now().minusDays(30));
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