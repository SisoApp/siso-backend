package com.siso.call.presentation;

import com.siso.call.CallService;
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

    // 1. 발신자 기준 통화 조회
    @GetMapping(value = "/caller", produces = "application/json; charset=UTF-8")
    public SisoResponse<List<CallResponseDto>> getCallsByCaller(@CurrentUser User caller) {
        return SisoResponse.success(callService.getCallsByCaller(caller));
    }

    // 2. 수신자 기준 통화 조회
    @GetMapping(value = "/receiver", produces = "application/json; charset=UTF-8")
    public SisoResponse<List<CallResponseDto>> getCallsByReceiver(@CurrentUser User receiver) {
        return SisoResponse.success(callService.getCallsByReceiver(receiver));
    }
}