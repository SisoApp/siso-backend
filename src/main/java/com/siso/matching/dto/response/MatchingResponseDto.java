package com.siso.matching.dto.response;

import com.siso.matching.doamain.model.Status;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingResponseDto {
    private boolean isLiked;
    private Status status;

    public MatchingResponseDto(boolean isLiked, Status status) {
        this.isLiked = isLiked;
        this.status = status;
    }
}
