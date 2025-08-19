package com.siso.like.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeResponseDto {
    private Boolean isLiked;
    private Boolean mutualLike;

    public LikeResponseDto(Boolean isLiked, Boolean mutualLike) {
        this.isLiked = isLiked;
        this.mutualLike = mutualLike;
    }
}
