package com.siso.call.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallRequestDto {
    private Long callerId;
    private Long receiverId;

    public CallRequestDto(Long callerId, Long receiverId) {
        this.callerId = callerId;
        this.receiverId = receiverId;
    }
}
