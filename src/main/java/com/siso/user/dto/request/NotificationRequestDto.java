package com.siso.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationRequestDto {
    @NotNull(message = "subscribed는 필수입니다.")
    private Boolean subscribed;
}
