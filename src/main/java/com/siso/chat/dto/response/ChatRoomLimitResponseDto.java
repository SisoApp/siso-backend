package com.siso.chat.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomLimitResponseDto {
    private Long chatRoomId;
    private Long userId;
    private int messageCount;

    public ChatRoomLimitResponseDto(Long chatRoomId, Long userId, int messageCount) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.messageCount = messageCount;
    }
}
