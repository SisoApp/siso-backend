package com.siso.like.dto.response;

import com.siso.like.doamain.model.Like;
import com.siso.like.doamain.model.LikeStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceivedLikeResponseDto {
    private Long senderId;
    private Long receiverId;
    private LikeStatus likeStatus;

    public ReceivedLikeResponseDto(Long senderId, Long receiverId, LikeStatus likeStatus) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.likeStatus = likeStatus;
    }
}
