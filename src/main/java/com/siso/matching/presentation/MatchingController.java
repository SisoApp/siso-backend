package com.siso.matching.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.matching.application.MatchingService;
import com.siso.matching.dto.response.MatchingResponseDto;
import com.siso.user.domain.model.User;

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
    public SisoResponse<List<MatchingResponseDto>> getReceivedMatchings(@CurrentUser User user) {
        List<MatchingResponseDto> receivedMatchings = matchingService.getReceivedMatchings(user);
        return SisoResponse.success(receivedMatchings);
    }

    // 매칭 취소
    @DeleteMapping("/{receiverId}")
    public SisoResponse<Void> deleteMatching(@CurrentUser User sender,
                                             @PathVariable Long receiverId) {
        matchingService.deleteMatching(sender, receiverId);
        return SisoResponse.success(null);
    }
}
