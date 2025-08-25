package com.siso.chat.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageResponseDto {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;

    public ChatMessageResponseDto(Long id, Long chatRoomId, Long senderId, String content, LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }
}
