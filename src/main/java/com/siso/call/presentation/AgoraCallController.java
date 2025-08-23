package com.siso.call.presentation;

import com.siso.call.application.AgoraCallService;
import com.siso.call.dto.CallInfoDto;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.call.dto.response.CallResponseDto;
import com.siso.common.response.SisoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calls")
public class AgoraCallController {
    private final AgoraCallService agoraCallService;

    // 1. 통화 요청
    @PostMapping("/request")
    public SisoResponse<CallInfoDto> requestCall(@RequestBody CallRequestDto requestDto) throws Exception {
        CallInfoDto callInfo = agoraCallService.requestCall(requestDto);
        return SisoResponse.success(callInfo);
    }

    // 2. 통화 수락
    @PostMapping("/accept")
    public SisoResponse<CallResponseDto> acceptCall(@RequestBody CallInfoDto callInfoDto) {
        CallResponseDto response = agoraCallService.acceptCall(callInfoDto);
        return SisoResponse.success(response);
    }

    // 3. 통화 거절
    @PostMapping("/deny")
    public SisoResponse<CallResponseDto> denyCall(@RequestBody CallInfoDto callInfoDto) {
        CallResponseDto response = agoraCallService.denyCall(callInfoDto);
        return SisoResponse.success(response);
    }

    // 4. 통화 종료
    @PostMapping("/end")
    public SisoResponse<CallResponseDto> endCall(@RequestBody CallInfoDto callInfoDto) {
        CallResponseDto response = agoraCallService.endCall(callInfoDto);
        return SisoResponse.success(response);
    }
}
