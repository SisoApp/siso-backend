package com.siso.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum RegistrationStatus {
    LOGIN("로그인"),
    REGISTER("회원 가입");

    private String registrationStatus;

    RegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }
}
