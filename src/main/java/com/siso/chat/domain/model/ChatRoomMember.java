package com.siso.chat.domain.model;

import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_room_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Long lastReadMessageId; // 읽음 처리

    @OneToOne(mappedBy = "chat_room_members", cascade = CascadeType.ALL, orphanRemoval = true)
    private ChatRoomLimit limit;

    @Builder
    public ChatRoomMember(ChatRoom chatRoom, User user, Long lastReadMessageId) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.lastReadMessageId = lastReadMessageId;
    }

    public void updateLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
}
