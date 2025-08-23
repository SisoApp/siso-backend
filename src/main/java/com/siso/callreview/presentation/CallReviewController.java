package com.siso.callreview.presentation;

import com.siso.callreview.application.CallReviewService;
import com.siso.callreview.dto.request.CallReviewRequestDto;
import com.siso.callreview.dto.response.CallReviewResponseDto;
import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/call-reviews")
@RequiredArgsConstructor
public class CallReviewController {
    private final CallReviewService callReviewService;

    // 리뷰 작성
    @PostMapping
    public SisoResponse<CallReviewResponseDto> createReview(@CurrentUser User evaluator,
                                                            @RequestBody CallReviewRequestDto request) {
        CallReviewResponseDto response = callReviewService.createReview(evaluator, request);
        return SisoResponse.success(response);
    }

    // 내 리뷰 조회
    @GetMapping("/my")
    public SisoResponse<CallReviewResponseDto> getMyReview(@RequestParam Long callId,
                                                           @CurrentUser User evaluator) {
        CallReviewResponseDto response = callReviewService.getMyReview(callId, evaluator);
        return SisoResponse.success(response);
    }

    // 상대방 리뷰 조회
    @GetMapping("/partner")
    public SisoResponse<CallReviewResponseDto> getPartnerReview(@RequestParam Long callId,
                                                                @CurrentUser User target) {
        CallReviewResponseDto response = callReviewService.getPartnerReview(callId, target);
        return SisoResponse.success(response);
    }
}

