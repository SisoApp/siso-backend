package com.siso.chat.dto.response;

import com.siso.chat.domain.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponseDto {

    private Long id;
    private Long senderId;
    private Long chatRoomId;
    private String content; // 원본 파일명
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean is_deleted;

    /**
     * Image 엔티티를 DTO로 변환
     */
    public static ChatMessageResponseDto fromEntity(ChatMessage chatMessage) {
        return ChatMessageResponseDto.builder()
                .id(chatMessage.getId())
                .senderId(chatMessage.getUser().getId())
                .content(chatMessage.getContent())
                .createdAt(chatMessage.getCreatedAt())
                .updatedAt(chatMessage.getUpdatedAt())
                .is_deleted(chatMessage.is_deleted())
                .build();
    }
}
