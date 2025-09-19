package com.siso.chat.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatListUpdateDto {
    private Long chatRoomId;
    private int unreadCount;

    public ChatListUpdateDto(Long chatRoomId, int unreadCount) {
        this.chatRoomId = chatRoomId;
        this.unreadCount = unreadCount;
    }
}
