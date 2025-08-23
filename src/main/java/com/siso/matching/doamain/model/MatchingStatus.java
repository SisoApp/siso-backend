package com.siso.matching.doamain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum MatchingStatus {
    PENDING("매칭 성립"),
    CALL_AVAILABLE("통화 가능"),
    CALLED("서로 통화 중"),
    AFTER("채팅으로 이동"),
    ENDED("통화 종료");

    private String MatchingStatus;

    MatchingStatus(String MatchingStatus) {
        this.MatchingStatus = MatchingStatus;
    }
}
