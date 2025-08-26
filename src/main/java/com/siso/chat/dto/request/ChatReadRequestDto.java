package com.siso.chat.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatReadRequestDto {
    private Long chatRoomId;
    private Long userId;
    private Long lastReadMessageId;
}
