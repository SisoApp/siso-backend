package com.siso.user.dto.response;

import com.siso.user.domain.model.RegistrationStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenResponseDto {
    private String refreshToken;
    private RegistrationStatus registrationStatus;

    public TokenResponseDto(String refreshToken, RegistrationStatus registrationStatus) {
        this.refreshToken = refreshToken;
        this.registrationStatus = registrationStatus;
    }
}
