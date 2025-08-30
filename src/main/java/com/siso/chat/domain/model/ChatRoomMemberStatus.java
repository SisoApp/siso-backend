package com.siso.chat.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum ChatRoomMemberStatus {
    JOINED("참여중"),
    LEFT("나감");

    private String chatRoomMemberStatus;

    ChatRoomMemberStatus(String chatRoomMemberStatus) {
        this.chatRoomMemberStatus = chatRoomMemberStatus;
    }
}
