package com.siso.call.dto.response;

import com.siso.call.domain.model.CallStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgoraCallResponseDto {
    private boolean accepted;
    private String token;
    private String channelName;;
    private Long callerId;
    private Long receiverId;
    private CallStatus callStatus;
    private Long duration; // 초 단위
    private UserProfileDto callerProfile;
    private UserProfileDto receiverProfile;

    public AgoraCallResponseDto(boolean accepted, String token, String channelName, Long callerId, Long receiverId, CallStatus callStatus, Long duration, UserProfileDto callerProfile, UserProfileDto receiverProfile) {
        this.accepted = accepted;
        this.token = token;
        this.channelName = channelName;
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.callStatus = callStatus;
        this.duration = duration;
        this.callerProfile = callerProfile;
        this.receiverProfile = receiverProfile;
    }
}
