package com.siso.call.application;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.domain.repository.CallRepository;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.call.dto.CallInfoDto;
import com.siso.call.dto.response.CallResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.matching.doamain.model.Matching;
import com.siso.matching.doamain.model.MatchingStatus;
import com.siso.matching.doamain.repository.MatchingRepository;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AgoraCallService {
    private final CallRepository callRepository;
    private final MatchingRepository matchingRepository;
    private final AgoraTokenService agoraTokenService;
    private final AgoraChannelNameService agoraChannelNameService;

    // 통화 요청(시작)
    public CallInfoDto requestCall(User caller, CallRequestDto request) throws Exception {
        Long callerId = caller.getId();
        Long receiverId = request.getReceiverId();

        // 매칭 조회
        Matching matching = matchingRepository.findByUsers(callerId, request.getReceiverId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.MATCHING_NOT_FOUND));

        // Agora 채널 생성
        String channelName = agoraChannelNameService.generateChannelName(callerId, receiverId);
        String token = agoraTokenService.generateToken(channelName, callerId);

        // Call 엔티티 생성 및 상태 업데이트
        Call call = Call.builder()
                .matching(matching)
                .callStatus(CallStatus.REQUESTED)
                .agoraChannelName(channelName)
                .agoraToken(token)
                .build();

        callRepository.save(call);

        return new CallInfoDto(call.getId(), channelName, token, callerId, receiverId);
    }

    // 통화 수락
    public CallResponseDto acceptCall(CallInfoDto callInfoDto) {
        Call call = getCall(callInfoDto);
        Matching matching = call.getMatching();

        // Call 상태 업데이트
        call.updateCallStatus(CallStatus.ACCEPT);
        call.startCall(); // duration, 시작 시간 초기화

        // Matching 상태 업데이트
        matching.updateStatus(MatchingStatus.CALLED);
        // User 상태 업데이트
        matching.getUser1().updatePresenceStatus(PresenceStatus.IN_CALL);
        matching.getUser2().updatePresenceStatus(PresenceStatus.IN_CALL);

        // 저장
        callRepository.save(call);
        matchingRepository.save(matching);

        return buildResponse(call, matching, true);
    }

    // 통화 취소
    public CallResponseDto denyCall(CallInfoDto callInfoDto) {
        Call call = getCall(callInfoDto);
        Matching matching = call.getMatching();

        // Call 상태 업데이트
        call.updateCallStatus(CallStatus.DENY);

        // Matching 상태를 다시 대기(PENDING)로 변경
        matching.updateStatus(MatchingStatus.PENDING);

        // 저장
        callRepository.save(call);
        matchingRepository.save(matching);

        return buildResponse(call, matching, false);
    }

    // 통화 종료
    public CallResponseDto endCall(CallInfoDto callInfoDto) {
        Call call = getCall(callInfoDto);;
        Matching matching = call.getMatching();

        call.endCall();
        matching.updateStatus(MatchingStatus.CALLED);

        callRepository.save(call);
        matchingRepository.save(matching);

        return buildResponse(call, matching, true);
    }

    private Call getCall(CallInfoDto callInfoDto) {
        return callRepository.findById(callInfoDto.getId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.CALL_NOT_FOUND));
    }

    private CallResponseDto buildResponse(Call call, Matching matching, boolean accepted) {
        return new CallResponseDto(
                accepted,
                call.getAgoraToken(),
                call.getAgoraChannelName(),
                matching.getUser1().getId(),
                matching.getUser2().getId(),
                call.getCallStatus(),
                call.getDuration()
        );
    }
}