package com.siso.chat.domain.model;

import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chatroom_sender", columnList = "chat_room_id, sender_id"),
    @Index(name = "idx_sender_id", columnList = "sender_id")
})
@EqualsAndHashCode(callSuper = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "deleted", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean deleted;

    // 양방향 연관 관계 설정
    public void linkChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        chatRoom.getChatMessages().add(this);
    }

    public void linkSender(User sender) {
        this.sender = sender;
    }

    @Builder public ChatMessage(String content, ChatRoom chatRoom, User sender) {
        this.content = content;
        linkChatRoom(chatRoom);
        linkSender(sender);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateDelete(boolean deleted) {
        this.deleted = deleted;
    }
}
