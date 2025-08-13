package com.siso.matching.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.matching.application.MatchingService;
import com.siso.matching.doamain.model.Matching;
import com.siso.matching.dto.request.MatchingRequestDto;
import com.siso.matching.dto.response.MatchingResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;

    @PostMapping("/like")
    public SisoResponse<MatchingResponseDto> updateLike(@RequestParam Long senderId,
                                                        @RequestParam Long receiverId,
                                                        @Valid @RequestBody MatchingRequestDto matchingRequestDto) {

        MatchingResponseDto matchingResponseDto = matchingService.updateLike(senderId, receiverId, matchingRequestDto);
        return SisoResponse.success(matchingResponseDto);
    }

    @PostMapping("/call-complete")
    public SisoResponse<MatchingResponseDto> completeCall(@RequestParam Long senderId,
                                                          @RequestParam Long receiverId) {

        MatchingResponseDto matchingResponseDto = matchingService.completeCall(senderId, receiverId);
        return SisoResponse.success(matchingResponseDto);
    }
}
