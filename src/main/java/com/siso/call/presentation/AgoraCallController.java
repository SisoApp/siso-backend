package com.siso.call.presentation;

import com.siso.call.application.AgoraCallService;
import com.siso.call.dto.CallInfoDto;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.call.dto.response.CallResponseDto;
import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calls")
public class AgoraCallController {
    private final AgoraCallService agoraCallService;

    // 1. 통화 요청
    @PostMapping("/request")
    public SisoResponse<CallInfoDto> requestCall(@CurrentUser User caller,
                                                 @Valid @RequestBody CallRequestDto requestDto) throws Exception {
        CallInfoDto callInfo = agoraCallService.requestCall(caller, requestDto);
        return SisoResponse.success(callInfo);
    }

    // 2. 통화 수락
    @PostMapping("/accept")
    public SisoResponse<CallResponseDto> acceptCall(@Valid @RequestBody CallInfoDto callInfoDto) {
        CallResponseDto response = agoraCallService.acceptCall(callInfoDto);
        return SisoResponse.success(response);
    }

    // 3. 통화 거절
    @PostMapping("/deny")
    public SisoResponse<CallResponseDto> denyCall(@Valid @RequestBody CallInfoDto callInfoDto) {
        CallResponseDto response = agoraCallService.denyCall(callInfoDto);
        return SisoResponse.success(response);
    }

    // 4. 통화 종료
    @PostMapping("/end")
    public SisoResponse<CallResponseDto> endCall(@Valid @RequestBody CallInfoDto callInfoDto,
                                                 @RequestParam boolean continueRelationship) {
        CallResponseDto response = agoraCallService.endCall(callInfoDto, continueRelationship);
        return SisoResponse.success(response);
    }
}