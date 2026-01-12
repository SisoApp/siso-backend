package com.siso.chat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequestDto {
    private Long chatRoomId;
    private String content;
}
