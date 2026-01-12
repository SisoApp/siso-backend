package com.siso.chat.application.event;

import com.siso.chat.dto.response.ChatMessageResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RabbitMQ로 전송할 채팅 메시지 이벤트
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEvent implements Serializable {
    private Long messageId;
    private Long chatRoomId;
    private Long senderId;
    private String content;
    private LocalDateTime timestamp;
    private List<Long> recipientUserIds;  // 메시지를 받을 사용자들 (본인 제외)
    private ChatMessageResponseDto message;  // 전체 메시지 DTO

    public static ChatMessageEvent from(ChatMessageResponseDto message, List<Long> recipientUserIds) {
        return new ChatMessageEvent(
                message.getId(),
                message.getChatRoomId(),
                message.getSenderId(),
                message.getContent(),
                message.getCreatedAt(),
                recipientUserIds,
                message
        );
    }
}
