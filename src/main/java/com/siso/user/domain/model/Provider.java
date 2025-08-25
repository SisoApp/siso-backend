package com.siso.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum Provider {
    KAKAO("카카오"),
    APPLE("애플");

    private String provider;

    Provider(String provider) {
        this.provider = provider;
    }
}
