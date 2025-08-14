package com.siso.user.dto.response;

import com.siso.user.domain.model.Provider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserResponseDto {
    private Provider provider;
    private String phoneNumber;

    public UserResponseDto(Provider provider, String phoneNumber) {
        this.provider = provider;
        this.phoneNumber = phoneNumber;
    }
}