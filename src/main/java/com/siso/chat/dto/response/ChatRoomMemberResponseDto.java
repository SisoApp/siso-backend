package com.siso.chat.dto.response;

import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.domain.model.ChatRoomMemberStatus;
import com.siso.image.domain.model.Image;

public record ChatRoomMemberResponseDto(
        Long id,
        Long userId,
        String nickName,
        Image image,
        ChatRoomMemberStatus status
) {
    public static ChatRoomMemberResponseDto fromEntity(ChatRoomMember member) {
        return new ChatRoomMemberResponseDto(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getUserProfile().getNickname(), // username 필드 존재 가정
                member.getUser().getUserProfile().getProfileImage(),
                member.getChatRoomMemberStatus()
        );
    }
}
