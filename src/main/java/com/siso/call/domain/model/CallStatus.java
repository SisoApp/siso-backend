package com.siso.call.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum CallStatus {
    REQUESTED("요청"),
    CANCELED("취소"),
    ACCEPT("승낙"),
    DENY("거절"),
    ENDED("종료");

    private String callStatus;

    CallStatus(String callStatus) {
        this.callStatus = callStatus;
    }
}