package com.siso.matching.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.matching.application.MatchingService;
import com.siso.matching.dto.response.MatchingResponseDto;
import com.siso.matching.dto.response.MatchingCandidateResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;

    // 받은 매칭 목록 조회
    @GetMapping("/received")
    public SisoResponse<List<MatchingResponseDto>> getReceivedMatchings(@RequestParam(name = "receiverId") Long receiverId) {
        List<MatchingResponseDto> receivedMatchings = matchingService.getReceivedMatchings(receiverId);
        return SisoResponse.success(receivedMatchings);
    }

    // 매칭 필터링된 사용자 목록 조회
    @GetMapping("/filter")
    public SisoResponse<List<MatchingCandidateResponseDto>> getFilteredMatches(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        List<MatchingCandidateResponseDto> filteredMatches = matchingService.getFilteredMatches(userId, limit);
        return SisoResponse.success(filteredMatches);
    }
}
