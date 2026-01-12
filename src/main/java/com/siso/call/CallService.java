package com.siso.call;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.repository.CallRepository;

import com.siso.call.dto.response.CallResponseDto;
import com.siso.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallService {
    private final CallRepository callRepository;

    // 발신자 기준 조회
    public List<CallResponseDto> getCallsByCaller(User caller) {
        return callRepository.findByCallerId(caller.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    // 수신자 기준 조회
    public List<CallResponseDto> getCallsByReceiver(User receiver) {
        return callRepository.findByReceiverId(receiver.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    // Call → AgoraCallResponseDto 변환
    private CallResponseDto toResponseDto(Call call) {
        return new CallResponseDto(
                true,
                call.getAgoraToken(),
                call.getAgoraChannelName(),
                call.getCaller().getId(),
                call.getReceiver().getId(),
                call.getCallStatus(),
                call.getDuration()
        );
    }
}