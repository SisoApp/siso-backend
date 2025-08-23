package com.siso.like.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum LikeStatus {
    ACTIVE("누른 상태"),
    CANCELED("취소됨");

    private String likeStatus;

    LikeStatus(String likeStatus) {
        this.likeStatus = likeStatus;
    }
}
