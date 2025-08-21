package com.siso.matching.doamain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum MatchingStatus {
    MATCHED("매칭 성사"),
    WAITING_CALL("통화 대기 중"),
    CALL_COMPLETED("통화 완료");

    private String status;

    MatchingStatus(String status) {
        this.status = status;
    }
}
