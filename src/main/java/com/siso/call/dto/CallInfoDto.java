package com.siso.call.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallInfoDto {
    private Long id;
    private String channelName;
    private String token;
    private Long callerId;
    private Long receiverId;
    private boolean firstCall;

    public CallInfoDto(Long id, String channelName, String token, Long callerId, Long receiverId, boolean firstCall) {
        this.id = id;
        this.channelName = channelName;
        this.token = token;
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.firstCall = firstCall;
    }
}
