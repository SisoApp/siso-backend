package com.siso.matching.dto.request;

import com.siso.user.domain.model.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingInfoDto {
    private User sender;
    private User receiver;

    public MatchingInfoDto(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }
}
