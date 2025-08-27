package com.siso.callreview.presentation;

import com.siso.callreview.application.CallReviewService;
import com.siso.callreview.dto.request.CallReviewRequestDto;
import com.siso.callreview.dto.response.CallReviewResponseDto;
import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.user.application.UserService;
import com.siso.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/call-reviews")
@RequiredArgsConstructor
public class CallReviewController {
    private final CallReviewService callReviewService;
    private final UserService userService;

    // 리뷰 작성
    @PostMapping
    public SisoResponse<CallReviewResponseDto> createReview(@CurrentUser User evaluator,
                                                            @Valid @RequestBody CallReviewRequestDto request) {
        CallReviewResponseDto response = callReviewService.createReview(evaluator, request);
        return SisoResponse.success(response);
    }

    // 평가 수정
    @PatchMapping
    public SisoResponse<CallReviewResponseDto> updateReview(@CurrentUser User evaluator,
                                                            @Valid @RequestBody CallReviewRequestDto request) {
        CallReviewResponseDto response = callReviewService.updateReview(evaluator, request);
        return SisoResponse.success(response);
    }

    // 내가 받은 평가 목록 조회
    @GetMapping("/received")
    public SisoResponse<List<CallReviewResponseDto>> getReceivedReviews(@CurrentUser User target) {
        List<CallReviewResponseDto> reviews = callReviewService.getReceivedReviews(target);
        return SisoResponse.success(reviews);
    }

    // 상대방이 받은 평가 목록 조회
    @GetMapping("/other/{userId}")
    public SisoResponse<List<CallReviewResponseDto>> getReviewsOfOtherUser(@RequestParam Long userId) {
        User otherUser = userService.getUserById(userId);
        List<CallReviewResponseDto> reviews = callReviewService.getReviewsOfOtherUser(otherUser);
        return SisoResponse.success(reviews);
    }

    // 내가 작성한 평가 목록 조회
    @GetMapping("/written")
    public SisoResponse<List<CallReviewResponseDto>> getMyWrittenReviews(@CurrentUser User evaluator) {
        List<CallReviewResponseDto> reviews = callReviewService.getMyWrittenReviews(evaluator);
        return SisoResponse.success(reviews);
    }
}
