package com.siso.user.dto.response;

import com.siso.user.domain.model.RegistrationStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private RegistrationStatus registrationStatus;
    private boolean hasProfile;

    public TokenResponseDto(String accessToken, String refreshToken, RegistrationStatus registrationStatus, boolean hasProfile) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.registrationStatus = registrationStatus;
        this.hasProfile = hasProfile;
    }
}