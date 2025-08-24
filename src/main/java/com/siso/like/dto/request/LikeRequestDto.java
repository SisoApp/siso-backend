package com.siso.like.dto.request;

import com.siso.like.doamain.model.LikeStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeRequestDto {
    private Long receiverId;
    private LikeStatus likeStatus;

    public LikeRequestDto(Long receiverId, LikeStatus likeStatus) {
        this.receiverId = receiverId;
        this.likeStatus = likeStatus;
    }
}