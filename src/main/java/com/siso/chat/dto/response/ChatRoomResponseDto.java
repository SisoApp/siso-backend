package com.siso.chat.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomResponseDto {
    private Long id;
    private String otherUserNickname;
    private String otherUserProfileImagePath;
    private int memberCount;
    private String lastMessageContent;
    private LocalDateTime lastMessageSentAt;
    private int unreadMessageCount;

    public  ChatRoomResponseDto(Long id, String otherUserNickname, String otherUserProfileImagePath, int memberCount, String lastMessageContent, LocalDateTime lastMessageSentAt, int unreadMessageCount) {
        this.id = id;
        this.otherUserNickname = otherUserNickname;
        this.otherUserProfileImagePath = otherUserProfileImagePath;
        this.memberCount = memberCount;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageSentAt = lastMessageSentAt;
        this.unreadMessageCount = unreadMessageCount;
    }
}
