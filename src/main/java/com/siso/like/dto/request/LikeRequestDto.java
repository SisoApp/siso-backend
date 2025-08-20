package com.siso.like.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeRequestDto {
    private Long receiverId;
    private boolean isLiked;

    public LikeRequestDto(Long receiverId, boolean isLiked) {
        this.receiverId = receiverId;
        this.isLiked = isLiked;
    }
}