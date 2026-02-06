package com.siso.call.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CallRequestDto {
    private Long receiverId;

    public CallRequestDto(Long receiverId) {
        this.receiverId = receiverId;
    }
}
