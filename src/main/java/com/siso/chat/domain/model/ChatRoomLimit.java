package com.siso.chat.domain.model;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_member")
    private ChatRoomMember chatRoomMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "sent_count")
    private int sentCount = 0;    // 보낸 메시지 수

    @Builder public ChatRoomLimit(ChatRoomMember chatRoomMember) {
        this.chatRoomMember = chatRoomMember;
        this.user = chatRoomMember.getUser();
        chatRoomMember.chatRoomLimit = this;
    }

    // 메시지 전송 횟수 5
    public boolean canSendMessage() {
        return sentCount <= 5;
    }

    // 메시지 전송 횟수 증가
    public void increaseCount() {
        if (!canSendMessage()) {
            throw new ExpectedException(ErrorCode.MESSAGE_LIMIT_EXCEEDED);
        }
        sentCount++;
    }

    // 메시지 전송 횟수 초기화
    public void resetSentCount() {
        this.sentCount = 0;
    }
}
