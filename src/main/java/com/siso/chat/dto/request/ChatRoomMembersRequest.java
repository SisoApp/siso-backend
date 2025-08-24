package com.siso.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMembersRequest {
    private Long id;
    private Long userId;
    private Long chatRoomId;
}
