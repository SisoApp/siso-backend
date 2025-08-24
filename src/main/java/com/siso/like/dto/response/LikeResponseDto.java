package com.siso.like.dto.response;

import com.siso.like.doamain.model.LikeStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeResponseDto {
    private LikeStatus likeStatus;
    private Boolean mutualLike;

    public LikeResponseDto(LikeStatus likeStatus, Boolean mutualLike) {
        this.likeStatus = likeStatus;
        this.mutualLike = mutualLike;
    }
}
