package com.siso.chat.domain.model;

import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_room_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ChatRoomMemberStatus chatRoomMemberStatus = ChatRoomMemberStatus.JOINED;

    private Long lastReadMessageId; // 읽음 처리

    @OneToOne(mappedBy = "chatRoomMember", cascade = CascadeType.ALL, orphanRemoval = true)
    protected ChatRoomLimit chatRoomLimit;

    // 양방향 연관 관계 설정
    public void linkChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        chatRoom.getChatRoomMembers().add(this);
    }

    public void linkUser(User user) {
        this.user = user;
        user.getChatRoomMembers().add(this);
    }

    @Builder
    public static ChatRoomMember of(User user, ChatRoom chatRoom) {
        ChatRoomMember member = new ChatRoomMember();
        member.linkUser(user);
        member.linkChatRoom(chatRoom);
        member.chatRoomLimit = new ChatRoomLimit(member); // messageLimit 5
        return member;
    }

    public boolean canSendMessage() {
        return chatRoomLimit.canSendMessage();
    }

    public void increaseMessageCount() {
        chatRoomLimit.increaseCount();
    }

    public void resetMessageCount() {
        chatRoomLimit.resetSentCount();
    }

    public void updateLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public void leave() {
        this.chatRoomMemberStatus = ChatRoomMemberStatus.LEFT;
    }
}
