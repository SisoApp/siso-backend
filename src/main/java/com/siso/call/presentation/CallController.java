package com.siso.call.presentation;

import com.siso.call.application.AgoraCallService;
import com.siso.call.application.CallService;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.call.dto.response.CallResponseDto;
import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {
    private final CallService callService;

    // 1. 특정 매칭의 통화 내역 조회
    @GetMapping("/matching/{matchingId}")
    public SisoResponse<List<CallResponseDto>> getCallsByMatching(@PathVariable Long matchingId) {
        return SisoResponse.success(callService.getCallsByMatching(matchingId));
    }

    // 2. 발신자 기준 통화 조회
    @GetMapping("/sender/{senderId}")
    public SisoResponse<List<CallResponseDto>> getCallsBySender(@CurrentUser User sender) {
        return SisoResponse.success(callService.getCallsBySender(sender));
    }

    // 3. 수신자 기준 통화 조회
    @GetMapping("/receiver/{receiverId}")
    public SisoResponse<List<CallResponseDto>> getCallsByReceiver(@CurrentUser User receiver) {
        return SisoResponse.success(callService.getCallsByReceiver(receiver));
    }

    // 9. 상태별 통화 조회
    @GetMapping("/status/{status}")
    public SisoResponse<List<CallResponseDto>> getCallsByStatus(@PathVariable CallStatus status) {
        return SisoResponse.success(callService.getCallsByStatus(status));
    }
}