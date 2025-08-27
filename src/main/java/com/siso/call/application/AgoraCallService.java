package com.siso.call.application;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.domain.repository.CallRepository;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.call.dto.CallInfoDto;
import com.siso.call.dto.response.CallResponseDto;
import com.siso.chat.application.ChatRoomService;
import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AgoraCallService {
    private final CallRepository callRepository;
    private final AgoraTokenService agoraTokenService;
    private final AgoraChannelNameService agoraChannelNameService;
    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 통화 요청
     */
    public CallInfoDto requestCall(User caller, CallRequestDto request) {
        Long receiverId = request.getReceiverId();

        // Agora 채널 생성
        String channelName = agoraChannelNameService.generateChannelName(caller.getId(), receiverId);
        String token = agoraTokenService.generateToken(channelName, caller.getId());

        Call call = Call.builder()
                .caller(caller)
                .callStatus(CallStatus.REQUESTED) // 통화 요청
                .agoraChannelName(channelName)
                .agoraToken(token)
                .startTime(LocalDateTime.now())
                .build();

        callRepository.save(call);

        return new CallInfoDto(call.getId(), channelName, token, caller.getId(), receiverId);
    }

    /**
     * 통화 수락
     */
    public CallResponseDto acceptCall(CallInfoDto callInfoDto) {
        Call call = getCall(callInfoDto.getCallerId());
        call.updateCallStatus(CallStatus.ACCEPT); // 통화 수락
        call.getCaller().updatePresenceStatus(PresenceStatus.IN_CALL);      // 사용자 상태 통화 중으로 변경
        call.getReceiver().updatePresenceStatus(PresenceStatus.IN_CALL);    // 사용자 상태 통화 중으로 변경
        call.startCall();
        callRepository.save(call);

        return buildResponse(call, true);
    }

    /**
     * 통화 거절
     */
    public CallResponseDto denyCall(CallInfoDto callInfoDto) {
        Call call = getCall(callInfoDto.getCallerId());
        call.updateCallStatus(CallStatus.DENY); // 통화 거절
        call.endCall();
        callRepository.save(call);

        return buildResponse(call, false);
    }

    /**
     * 통화 종료 → 이어가기 선택 시 채팅방 생성
     */
    public CallResponseDto endCall(CallInfoDto callInfoDto, boolean continueRelationship) {
        Call call = getCall(callInfoDto.getId());

        // 최초 통화인지 확인
        boolean isFirstCallLimited = true;
        ChatRoom chatRoom = chatRoomRepository.findByCallId(call.getId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.CHATROOM_NOT_FOUND));

        if (chatRoom != null && continueRelationship) {
            chatRoomService.unlockChatRoom(chatRoom); // 채팅 제한 해제
            call.updateCallStatus(CallStatus.ENDED); // 전화 무제한
            isFirstCallLimited = false; // 이어가기 후 제한 해제
        }

        // 종료 처리
        call.endCall(isFirstCallLimited);
        callRepository.save(call);

        return buildResponse(call, true);
    }

    /**
     * 내부 공용 메서드
     */
    private Call getCall(Long callId) {
        return callRepository.findById(callId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.CALL_NOT_FOUND));
    }

    private CallResponseDto buildResponse(Call call, boolean accepted) {
        return new CallResponseDto(
                accepted,
                call.getAgoraToken(),
                call.getAgoraChannelName(),
                call.getCaller().getId(),
                call.getReceiver().getId(),
                call.getCallStatus(),
                call.getDuration()
        );
    }
}