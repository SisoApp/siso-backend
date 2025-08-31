package com.siso.chat.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageRequestDto {
    private Long chatRoomId;
    private String content;
}
