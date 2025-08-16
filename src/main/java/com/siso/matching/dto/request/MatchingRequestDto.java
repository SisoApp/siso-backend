package com.siso.matching.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingRequestDto {
    private Long senderId;
    private Long receiverId;
    @NotNull
    private boolean isLiked;

    public MatchingRequestDto(Long senderId, Long receiverId, boolean isLiked) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.isLiked = isLiked;
    }
}
