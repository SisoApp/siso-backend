package com.siso.call.application;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.domain.repository.CallRepository;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.call.dto.CallInfoDto;
import com.siso.call.dto.response.CallResponseDto;
import com.siso.chat.application.ChatRoomService;
import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.domain.model.ChatRoomStatus;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.notification.application.NotificationService;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AgoraCallService {
    private final CallRepository callRepository;
    private final AgoraTokenService agoraTokenService;
    private final AgoraChannelNameService agoraChannelNameService;
    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 통화 요청
     */
    // 전화중 상대 요청 안됨
    public CallInfoDto requestCall(User caller, CallRequestDto request) {
        Long receiverId = request.getReceiverId();
        User receiver = findById(receiverId);

        // receiver가 이미 통화 중(IN_CALL)인지 체크
        if (receiver.getPresenceStatus() == PresenceStatus.IN_CALL) {
            throw new ExpectedException(ErrorCode.USER_IN_CALL);
        }

        // Agora 채널 생성
        String channelName = agoraChannelNameService.generateChannelName(caller.getId(), receiverId);
        String token = agoraTokenService.generateToken(channelName, caller.getId());

        // 최초 통화 여부 판단
        boolean firstCall = callRepository.findFirstByCallerIdAndReceiverIdOrderByStartTimeAsc(caller.getId(), receiverId).isEmpty();

        Call call = Call.builder()
                .caller(caller)
                .receiver(receiver)
                .callStatus(CallStatus.REQUESTED) // 통화 요청
                .agoraChannelName(channelName)
                .agoraToken(token)
                .startTime(LocalDateTime.now())
                .build();

        callRepository.save(call);

        // 수신자에게 통화 알림 전송
        sendCallNotificationToReceiver(call, caller, receiverId);

        return new CallInfoDto(call.getId(), channelName, token, caller.getId(), receiverId, firstCall);
    }

    /**
     * 통화 수락
     */
    public CallResponseDto acceptCall(CallInfoDto callInfoDto) {
        Call call = getCall(callInfoDto.getId());
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
        Call call = getCall(callInfoDto.getId());
        call.updateCallStatus(CallStatus.DENY); // 통화 거절
        call.endCall();
        callRepository.save(call);

        return buildResponse(call, false);
    }

    /**
     * 통화 종료 → 이어가기 선택 시 채팅방 생성
     */
    public CallResponseDto endCall(CallInfoDto callInfoDto, boolean continueRelationship) {
        // 1. 통화 정보 조회
        Call call = getCall(callInfoDto.getId());
        User caller = findById(callInfoDto.getCallerId());
        User receiver = findById(callInfoDto.getReceiverId());

        // 2. 통화 종료 상태 업데이트
        call.endCall();
        call.updateCallStatus(CallStatus.ENDED);
        callRepository.save(call);

        // 3. 최초 통화 + continueRelationship 처리
        if (callInfoDto.isFirstCall() && continueRelationship) {
            // ChatRoom 생성 또는 기존 조회
            ChatRoom chatRoom = chatRoomRepository.findByCallId(call.getId())
                    .orElseGet(() -> chatRoomRepository.saveAndFlush(
                            new ChatRoom(call, ChatRoomStatus.LIMITED)
                    ));

            // ChatRoomMember 생성 및 저장
            ChatRoomMember callerMember = ChatRoomMember.of(caller, chatRoom);
            ChatRoomMember receiverMember = ChatRoomMember.of(receiver, chatRoom);
            chatRoomMemberRepository.saveAll(List.of(callerMember, receiverMember));

            // 메시지 제한 5회 초기화 (LIMITED 상태)
            callerMember.resetMessageCount();
            receiverMember.resetMessageCount();
        }

        // 4. DTO 반환
        return buildResponse(call, continueRelationship);
    }

    /**
     * 수신자에게 통화 알림 전송 (발신자 제외)
     */
    private void sendCallNotificationToReceiver(Call call, User caller, Long receiverId) {
        try {
            // 발신자 닉네임 가져오기
            String callerNickname = caller.getUserProfile() != null 
                ? caller.getUserProfile().getNickname() 
                : "익명";
            
            // 발신자와 수신자가 다른 경우에만 알림 전송 (본인 제외)
            if (!caller.getId().equals(receiverId)) {
                // 발신자 프로필 이미지 가져오기
                String callerImage = caller.getUserProfile() != null && caller.getUserProfile().getProfileImage() != null
                    ? caller.getUserProfile().getProfileImage().getPath()
                    : "";
                
                notificationService.sendCallNotification(
                    receiverId,
                    caller.getId(),
                    callerNickname,
                    call.getId(),
                    call.getAgoraChannelName(),
                    call.getAgoraToken(),
                    callerImage
                );
                log.info("Call notification with details sent to user: {} from caller: {}, callId: {}", 
                        receiverId, caller.getId(), call.getId());
            } else {
                log.warn("Attempted to send call notification to self: {}", caller.getId());
            }
        } catch (Exception e) {
            // 알림 전송 실패가 통화 요청을 방해하지 않도록 예외를 잡음
            log.warn("Failed to send call notification to user {}: {}", receiverId, e.getMessage());
        }
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