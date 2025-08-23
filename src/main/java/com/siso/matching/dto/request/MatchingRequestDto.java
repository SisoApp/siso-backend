package com.siso.matching.dto.request;

import com.siso.user.domain.model.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingRequestDto {
    private User user1;
    private User user2;

    public MatchingRequestDto(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }
}
