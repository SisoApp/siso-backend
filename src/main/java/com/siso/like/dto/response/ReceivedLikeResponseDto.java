package com.siso.like.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceivedLikeResponseDto {
    private Long senderId;
    private Long receiverId;
    private Boolean isLiked;

    public ReceivedLikeResponseDto(Long senderId, Long receiverId, Boolean isLiked) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.isLiked = isLiked;
    }

}
