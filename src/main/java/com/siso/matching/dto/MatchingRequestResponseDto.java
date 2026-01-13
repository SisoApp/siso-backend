package com.siso.matching.dto;

import com.siso.matching.domain.model.MatchingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매칭 요청 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchingRequestResponseDto {
    private String requestId;
    private MatchingStatus status;
    private String message;
}
