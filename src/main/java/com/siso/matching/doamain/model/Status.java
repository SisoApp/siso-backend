package com.siso.matching.doamain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum Status {
    MATCHED("매칭됨"),
    CALL_COMPLETED("통화 완료"),
    REPORTED("신고됨"),
    BLOCKED("차단됨");

    private String status;

    Status(String status) {
        this.status = status;
    }
}
