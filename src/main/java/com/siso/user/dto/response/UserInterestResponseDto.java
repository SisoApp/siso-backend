package com.siso.user.dto.response;

import com.siso.user.domain.model.Interest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterestResponseDto {
    private Interest interest;

    public UserInterestResponseDto(Interest interest) {
        this.interest = interest;
    }
}
