package com.siso.matching.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingRequestDto {
    private boolean isLiked;

    public MatchingRequestDto(boolean isLiked) {
        this.isLiked = isLiked;
    }
}
