package com.siso.call.presentation;

import com.siso.call.application.CallService;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.call.dto.response.CallResponseDto;
import com.siso.call.dto.response.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/call")
@RequiredArgsConstructor
public class CallController {
    private final CallService callService;

    // 통화 요청
    @PostMapping("/request")
    public TokenResponseDto requestCall(@RequestBody CallRequestDto request) throws Exception {
        return callService.requestCall(request);
    }

    // 통화 수락/거절
    @PostMapping("/respond")
    public CallResponseDto respondCall(@RequestParam Long callId, @RequestParam CallStatus status) {
        return callService.respondCall(callId, status);
    }

    // 통화 종료
    @PostMapping("/end")
    public CallResponseDto endCall(@RequestParam Long callId) {
        return callService.endCall(callId);
    }
}