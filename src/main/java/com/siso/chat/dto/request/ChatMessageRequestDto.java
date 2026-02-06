package com.siso.chat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequestDto {
    private Long chatRoomId;
    private String content;

    public ChatMessageRequestDto(Long chatRoomId, String content) {
        this.chatRoomId = chatRoomId;
        this.content = content;
    }

    public void assignChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}
