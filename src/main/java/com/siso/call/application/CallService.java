package com.siso.call.application;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.domain.repository.CallRepository;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.call.dto.response.CallResponseDto;
import com.siso.call.dto.response.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CallService {

    private final CallRepository callRepository;
    private final AgoraTokenService agoraTokenService;
    private final ChannelService channelService;

    // 통화 요청(시작)
    public TokenResponseDto requestCall(CallRequestDto request) throws Exception {
        String channelName = channelService.generateChannelName(request.getSenderId(), request.getSenderId());
        String token = agoraTokenService.generateToken(channelName, request.getSenderId().intValue(), 3600);

        Call call = Call.builder().build();
        call.startCall(channelName, token); // 엔티티 내 메서드로 상태 초기화
        callRepository.save(call);

        return TokenResponseDto.builder()
                .token(token)
                .channelName(channelName)
                .build();
    }

    // 통화 수락/거절 처리
    public CallResponseDto respondCall(Long callId, CallStatus status) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Call not found"));

        call.updateCallStatus(status); // 엔티티 내 메서드로 상태 변경
        callRepository.save(call);

        return CallResponseDto.builder()
                .id(call.getId())
                .channelName(call.getAgoraChannelName())
                .callStatus(call.getCallStatus())
                .duration(call.getDuration())
                .build();
    }

    // 통화 종료 처리
    public CallResponseDto endCall(Long callId) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Call not found"));

        call.endCall(); // 엔티티 내 메서드로 종료 처리
        callRepository.save(call);

        return CallResponseDto.builder()
                .id(call.getId())
                .channelName(call.getAgoraChannelName())
                .callStatus(call.getCallStatus())
                .duration(call.getDuration())
                .build();
    }
}