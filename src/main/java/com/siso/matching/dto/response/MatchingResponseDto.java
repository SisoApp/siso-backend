package com.siso.matching.dto.response;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingResponseDto {
    private Long senderId;
    private Long receiverId;
    private String status;
    private LocalDateTime createdAt;

    @Builder
    public MatchingResponseDto(Long senderId, Long receiverId, String status, LocalDateTime createdAt) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.createdAt = createdAt;
    }
}
