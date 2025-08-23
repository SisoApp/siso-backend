package com.siso.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum PresenceStatus {
    OFFLINE("오프라인"),
    ONLINE("온라인"),
    IN_CALL("통화중");

    private String presenceStatus;

    PresenceStatus(String presenceStatus) {
        this.presenceStatus = presenceStatus;
    }
}
