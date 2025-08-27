package com.siso.chat.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum ChatRoomStatus {
    LIMITED("채팅 5회 제한, 전화 불가"),
    MATCHED("채팅 무제한, 전화 가능");

    private String chatStatus;

    ChatRoomStatus(String chatStatus) {
        this.chatStatus = chatStatus;
    }
}
