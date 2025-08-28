package com.siso.chat.dto.response;

import com.siso.image.domain.model.Image;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomResponseDto {
    private Long id;
    private String otherUserNickname;
    private Image otherUserProfileImage;
    private int memberCount;
    private String lastMessageContent;
    private LocalDateTime lastMessageSentAt;
    private int unreadMessageCount;

    public  ChatRoomResponseDto(Long id, String otherUserNickname, Image otherUserProfileImage, int memberCount, String lastMessageContent, LocalDateTime lastMessageSentAt, int unreadMessageCount) {
        this.id = id;
        this.otherUserNickname = otherUserNickname;
        this.otherUserProfileImage = otherUserProfileImage;
        this.memberCount = memberCount;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageSentAt = lastMessageSentAt;
        this.unreadMessageCount = unreadMessageCount;
    }
}
