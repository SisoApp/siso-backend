package com.siso.matching.dto.response;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingResponseDto {
    private Long user1Id;
    private Long user2Id;
    private String status;
    private LocalDateTime createdAt;

    public MatchingResponseDto(Long user1Id, Long user2Id, String status, LocalDateTime createdAt) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.status = status;
        this.createdAt = createdAt;
    }
}
