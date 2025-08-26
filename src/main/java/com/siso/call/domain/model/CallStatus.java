package com.siso.call.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum CallStatus {
    REQUESTED("통화 요청"),
    ACCEPT("승낙"),
    DENY("거절"),
    ENDED("통화 종료");

    private String callStatus;

    CallStatus(String callStatus) {
        this.callStatus = callStatus;
    }
}