package com.siso.call.dto.response;

import com.siso.call.domain.model.CallStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallResponseDto {
    private Long id;
    private String channelName;
    private CallStatus callStatus;
    private Long duration; // 초 단위

    @Builder
    public CallResponseDto(Long id, String channelName, CallStatus callStatus, Long duration) {
        this.id = id;
        this.channelName = channelName;
        this.callStatus = callStatus;
        this.duration = duration;
    }
}
