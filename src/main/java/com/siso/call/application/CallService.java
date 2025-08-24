package com.siso.call.application;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
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

    // 특정 매칭의 통화 내역 조회
    public List<CallResponseDto> getCallsByMatching(Long matchingId) {
        return callRepository.findByMatchingId(matchingId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    // 발신자 기준 조회
    public List<CallResponseDto> getCallsBySender(User sender) {
        return callRepository.findByCallerId(sender.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    // 수신자 기준 조회
    public List<CallResponseDto> getCallsByReceiver(User receiver) {
        return callRepository.findByReceiverId(receiver.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    // 상태별 통화 조회
    public List<CallResponseDto> getCallsByStatus(CallStatus callStatus) {
        return callRepository.findByCallStatus(callStatus).stream()
                .map(this::toResponseDto)
                .toList();
    }

    // Call → CallResponseDto 변환
    private CallResponseDto toResponseDto(Call call) {
        return new CallResponseDto(
                true,
                call.getAgoraToken(),
                call.getAgoraChannelName(),
                call.getMatching().getUser1().getId(),
                call.getMatching().getUser2().getId(),
                call.getCallStatus(),
                call.getDuration()
        );
    }
}
