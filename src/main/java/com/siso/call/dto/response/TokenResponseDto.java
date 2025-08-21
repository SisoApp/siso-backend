package com.siso.call.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenResponseDto {
    private String token;
    private String channelName;

    @Builder
    public TokenResponseDto(String token, String channelName) {
        this.token = token;
        this.channelName = channelName;
    }
}
