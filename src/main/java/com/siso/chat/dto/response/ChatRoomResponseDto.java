package com.siso.chat.dto.response;

import com.siso.call.domain.model.Call;
import com.siso.chat.domain.model.ChatMessage;
import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.dto.request.ChatRoomRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponseDto {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatRoomResponseDto fromEntity(ChatRoom chatRoom) {
        return ChatRoomResponseDto.builder()
                .id(chatRoom.getId())
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .build();
    }
}
