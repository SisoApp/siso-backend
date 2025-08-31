package com.siso.chat.dto.response;

import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.domain.model.ChatRoomMemberStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;

import java.util.Optional;

public record ChatRoomMemberResponseDto(
        Long id,
        Long userId,
        String nickName,
        String imagePath,
        ChatRoomMemberStatus status
) {
    public static ChatRoomMemberResponseDto fromEntity(ChatRoomMember member) {
        // 프로필 이미지 첫 번째 사용
        String profileImage = Optional.ofNullable(member.getUser())
                .map(User::getImages)
                .filter(images -> !images.isEmpty())
                .map(images -> images.get(0).getPath())
                .orElse("");

        // 닉네임 안전 처리
        String nickname = Optional.ofNullable(member.getUser())
                .map(User::getUserProfile)
                .map(UserProfile::getNickname)
                .orElse("익명");

        return new ChatRoomMemberResponseDto(
                member.getId(),
                member.getUser().getId(),
                nickname,
                profileImage,
                member.getChatRoomMemberStatus()
        );
    }
}
