package com.siso.call.presentation;

import com.siso.call.application.AgoraCallService;
import com.siso.call.dto.CallInfoDto;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.call.dto.response.AgoraCallResponseDto;
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
    @PostMapping(value = "/request", produces = "application/json; charset=UTF-8")
    public SisoResponse<CallInfoDto> requestCall(@CurrentUser User caller,
                                                 @Valid @RequestBody CallRequestDto requestDto) {
        CallInfoDto callInfo = agoraCallService.requestCall(caller, requestDto);
        return SisoResponse.success(callInfo);
    }

    // 2. 통화 수락
    @PostMapping(value = "/accept", produces = "application/json; charset=UTF-8")
    public SisoResponse<AgoraCallResponseDto> acceptCall(@Valid @RequestBody CallInfoDto callInfoDto) {
        AgoraCallResponseDto response = agoraCallService.acceptCall(callInfoDto);
        return SisoResponse.success(response);
    }

    // 3. 통화 거절
    @PostMapping(value = "/deny", produces = "application/json; charset=UTF-8")
    public SisoResponse<AgoraCallResponseDto> denyCall(@Valid @RequestBody CallInfoDto callInfoDto) {
        AgoraCallResponseDto response = agoraCallService.denyCall(callInfoDto);
        return SisoResponse.success(response);
    }

    // 4. 통화 종료
    @PostMapping(value = "/end", produces = "application/json; charset=UTF-8")
    public SisoResponse<AgoraCallResponseDto> endCall(@Valid @RequestBody CallInfoDto callInfoDto,
                                                      @RequestParam boolean continueRelationship) {
        AgoraCallResponseDto response = agoraCallService.endCall(callInfoDto, continueRelationship);
        return SisoResponse.success(response);
    }

    // 5. 통화 취소
    @PostMapping(value = "/cancel", produces = "application/json; charset=UTF-8")
    public SisoResponse<AgoraCallResponseDto> cancelCall(@CurrentUser User user,
                                                         @RequestParam Long callId) {
        AgoraCallResponseDto response = agoraCallService.cancelCall(user, callId);
        return SisoResponse.success(response);
    }
}