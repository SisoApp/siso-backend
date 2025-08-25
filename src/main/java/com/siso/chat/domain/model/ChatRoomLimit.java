package com.siso.chat.domain.model;

import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int messageCount = 0; // 보낸 메세지 수

    @Builder
    public ChatRoomLimit(ChatRoom chatRoom, User user, int messageCount) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.messageCount = messageCount;
    }

    // 메시지 전송 횟수 증가
    public void incrementMessageCount() {
        this.messageCount += 1;
    }

    // 메시지 전송 횟수 초기화
    public void resetMessageCount() {
        this.messageCount = 0;
    }
}
