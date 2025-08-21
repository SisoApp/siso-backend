package com.siso.user.dto.request;

import com.siso.user.domain.model.Interest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterestRequestDto {
    private Interest interest;
}
