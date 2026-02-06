package com.siso.unitTest.service;

import com.siso.call.application.AgoraCallService;
import com.siso.call.application.AgoraChannelNameService;
import com.siso.call.application.AgoraTokenService;
import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.domain.repository.CallRepository;
import com.siso.call.dto.CallInfoDto;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.call.dto.response.AgoraCallResponseDto;
import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.domain.model.ChatRoomStatus;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.notification.application.NotificationService;
import com.siso.notification.domain.model.Notification;
import com.siso.user.domain.model.*;
import com.siso.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AgoraCallService 단위 테스트
 *
 * 테스트 대상:
 * - 통화 요청 성공
 * - 통화 요청 실패 (수신자가 이미 통화 중)
 * - 통화 수락
 * - 통화 거절
 * - 통화 취소
 * - 통화 종료
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgoraCallService 단위 테스트")
class AgoraCallServiceTest {

    @Mock
    private CallRepository callRepository;

    @Mock
    private AgoraTokenService agoraTokenService;

    @Mock
    private AgoraChannelNameService agoraChannelNameService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AgoraCallService agoraCallService;

    private User caller;
    private User receiver;

    private static final String TEST_CHANNEL_NAME = "test-channel-123";
    private static final String TEST_AGORA_TOKEN = "test-agora-token";

    @BeforeEach
    void setUp() {
        // Given: 발신자와 수신자 설정
        caller = createUser(1L, "caller@test.com", "010-1111-2222");
        receiver = createUser(2L, "receiver@test.com", "010-3333-4444");
    }

    @Test
    @DisplayName("통화 요청 성공")
    void requestCall_whenReceiverIsOnline_shouldCreateCall() {
        // Given
        Long receiverId = 2L;
        CallRequestDto request = new CallRequestDto(receiverId);

        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(agoraChannelNameService.generateChannelName(any(), any())).thenReturn(TEST_CHANNEL_NAME);
        when(agoraTokenService.generateToken(TEST_CHANNEL_NAME)).thenReturn(TEST_AGORA_TOKEN);

        Call savedCall = Call.builder()
                .caller(caller)
                .receiver(receiver)
                .callStatus(CallStatus.REQUESTED)
                .agoraChannelName(TEST_CHANNEL_NAME)
                .agoraToken(TEST_AGORA_TOKEN)
                .startTime(LocalDateTime.now())
                .build();

        when(callRepository.save(any(Call.class))).thenReturn(savedCall);
        when(notificationService.sendCallNotification(any(), any(), any(), any(), any(), any(), any())).thenReturn(mock(Notification.class));

        // When
        CallInfoDto result = agoraCallService.requestCall(caller, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getChannelName()).isEqualTo(TEST_CHANNEL_NAME);
        assertThat(result.getToken()).isEqualTo(TEST_AGORA_TOKEN);

        verify(userRepository).findById(receiverId);
        verify(agoraChannelNameService).generateChannelName(any(), any());
        verify(agoraTokenService).generateToken(TEST_CHANNEL_NAME);
        verify(callRepository).save(any(Call.class));
    }

    @Test
    @DisplayName("통화 요청 실패 - 수신자가 이미 통화 중")
    void requestCall_whenReceiverInCall_shouldThrowException() {
        // Given: 수신자가 이미 통화 중
        receiver.updatePresenceStatus(PresenceStatus.IN_CALL);
        Long receiverId = 2L;
        CallRequestDto request = new CallRequestDto(receiverId);

        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        // When & Then: 예외 발생
        assertThatThrownBy(() -> agoraCallService.requestCall(caller, request))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_IN_CALL);

        verify(userRepository).findById(receiverId);
        verify(callRepository, never()).save(any());
    }

    @Test
    @DisplayName("통화 요청 실패 - 수신자를 찾을 수 없음")
    void requestCall_whenReceiverNotFound_shouldThrowException() {
        // Given
        Long receiverId = 999L;
        CallRequestDto request = new CallRequestDto(receiverId);

        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> agoraCallService.requestCall(caller, request))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findById(receiverId);
    }

    @Test
    @DisplayName("통화 수락 성공")
    void acceptCall_shouldUpdateCallStatusToAccept() {
        // Given
        Call call = Call.builder()
                .caller(caller)
                .receiver(receiver)
                .callStatus(CallStatus.REQUESTED)
                .agoraChannelName(TEST_CHANNEL_NAME)
                .agoraToken(TEST_AGORA_TOKEN)
                .startTime(LocalDateTime.now())
                .build();

        CallInfoDto callInfoDto = new CallInfoDto(1L, TEST_CHANNEL_NAME, TEST_AGORA_TOKEN, 1L, 2L);

        when(callRepository.findById(1L)).thenReturn(Optional.of(call));
        when(callRepository.save(any(Call.class))).thenReturn(call);
        when(notificationService.sendCallAcceptedNotification(any(), any(), any(), any())).thenReturn(mock(Notification.class));

        // When
        AgoraCallResponseDto result = agoraCallService.acceptCall(callInfoDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAccepted()).isTrue();
        assertThat(call.getCallStatus()).isEqualTo(CallStatus.ACCEPT);

        verify(callRepository).findById(1L);
        verify(callRepository).save(call);
        verify(notificationService).sendCallAcceptedNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("통화 거절 성공(통화 상태 종료)")
    void denyCall_shouldUpdateCallStatusToDeny() {
        // Given
        Call call = Call.builder()
                .caller(caller)
                .receiver(receiver)
                .callStatus(CallStatus.REQUESTED)
                .agoraChannelName(TEST_CHANNEL_NAME)
                .agoraToken(TEST_AGORA_TOKEN)
                .startTime(LocalDateTime.now())
                .build();

        CallInfoDto callInfoDto = new CallInfoDto(1L, TEST_CHANNEL_NAME, TEST_AGORA_TOKEN, 1L, 2L);

        when(callRepository.findById(1L)).thenReturn(Optional.of(call));
        when(callRepository.save(any(Call.class))).thenReturn(call);
        when(notificationService.sendCallDeniedNotification(any(), any())).thenReturn(mock(Notification.class));

        // When
        AgoraCallResponseDto result = agoraCallService.denyCall(callInfoDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAccepted()).isFalse();
        assertThat(call.getCallStatus()).isEqualTo(CallStatus.ENDED);

        verify(callRepository).findById(1L);
        verify(callRepository).save(call);
        verify(notificationService).sendCallDeniedNotification(any(), any());
    }

    @Test
    @DisplayName("통화 거절 성공(통화 상태 거절)")
    void deny_shouldSetStatusToDeny() {
        // Given
        Call call = Call.builder()
                .callStatus(CallStatus.REQUESTED)
                .build();

        // when
        call.updateCallStatus(CallStatus.DENY);

        // then
        assertThat(call.getCallStatus()).isEqualTo(CallStatus.DENY);
    }

    @Test
    @DisplayName("통화 취소 성공(통화 상태 종료) - 발신자만 취소 가능")
    void cancelCall_whenCallerCancels_shouldUpdateStatus() {
        // Given
        Long callId = 1L;

        Call call = Call.builder()
                .caller(caller)
                .receiver(receiver)
                .callStatus(CallStatus.REQUESTED)
                .agoraChannelName(TEST_CHANNEL_NAME)
                .agoraToken(TEST_AGORA_TOKEN)
                .startTime(LocalDateTime.now())
                .build();

        // Caller에 ID 설정 (Reflection 사용)
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(caller, 1L);
            idField.set(receiver, 2L);
        } catch (Exception e) {
            // ID 설정 실패 시 무시
        }

        when(callRepository.findById(callId)).thenReturn(Optional.of(call));
        when(callRepository.save(any(Call.class))).thenReturn(call);
        when(notificationService.sendCallCanceledNotification(any(), any(), any())).thenReturn(mock(Notification.class));

        // When
        AgoraCallResponseDto result = agoraCallService.cancelCall(caller, callId);

        // Then
        assertThat(result).isNotNull();
        assertThat(call.getCallStatus()).isEqualTo(CallStatus.ENDED);

        verify(callRepository).findById(callId);
        verify(callRepository).save(call);
        verify(notificationService).sendCallCanceledNotification(any(), any(), any());
    }

    @Test
    @DisplayName("통화 취소 성공(통화 상태 취소)")
    void cancel_shouldSetStatusToCanceled() {
        // Given
        Call call = Call.builder()
                .callStatus(CallStatus.REQUESTED)
                .build();

        // When
        call.updateCallStatus(CallStatus.CANCELED);

        // Then
        assertThat(call.getCallStatus()).isEqualTo(CallStatus.CANCELED);
    }

    @Test
    @DisplayName("통화 취소 실패 - 수신자가 취소 시도")
    void cancelCall_whenReceiverCancels_shouldThrowException() {
        // Given
        Long callId = 1L;

        Call call = Call.builder()
                .caller(caller)
                .receiver(receiver)
                .callStatus(CallStatus.REQUESTED)
                .agoraChannelName(TEST_CHANNEL_NAME)
                .agoraToken(TEST_AGORA_TOKEN)
                .startTime(LocalDateTime.now())
                .build();

        // ID 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(caller, 1L);
            idField.set(receiver, 2L);
        } catch (Exception e) {
        }

        when(callRepository.findById(callId)).thenReturn(Optional.of(call));

        // When & Then: 수신자가 취소하면 예외 발생
        assertThatThrownBy(() -> agoraCallService.cancelCall(receiver, callId))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);

        verify(callRepository).findById(callId);
        verify(callRepository, never()).save(any());
    }

    @Test
    @DisplayName("통화 종료 성공 - 채팅방 생성 안함")
    void endCall_whenNotContinueRelationship_shouldNotCreateChatRoom() {
        // Given
        Call call = Call.builder()
                .caller(caller)
                .receiver(receiver)
                .callStatus(CallStatus.ACCEPT)
                .agoraChannelName(TEST_CHANNEL_NAME)
                .agoraToken(TEST_AGORA_TOKEN)
                .startTime(LocalDateTime.now())
                .build();

        CallInfoDto callInfoDto = new CallInfoDto(1L, TEST_CHANNEL_NAME, TEST_AGORA_TOKEN, 1L, 2L);

        when(callRepository.findById(1L)).thenReturn(Optional.of(call));
        when(userRepository.findById(1L)).thenReturn(Optional.of(caller));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(callRepository.save(any(Call.class))).thenReturn(call);
        when(chatRoomRepository.existsByMembers(caller, receiver)).thenReturn(false);

        // When: continueRelationship = false
        AgoraCallResponseDto result = agoraCallService.endCall(callInfoDto, false);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAccepted()).isFalse();
        assertThat(call.getCallStatus()).isEqualTo(CallStatus.ENDED);

        verify(callRepository).save(call);
        verify(chatRoomRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("통화 종료 성공 - 채팅방 생성")
    void endCall_whenContinueRelationship_shouldCreateChatRoom() {
        // Given
        Call call = Call.builder()
                .caller(caller)
                .receiver(receiver)
                .callStatus(CallStatus.ACCEPT)
                .agoraChannelName(TEST_CHANNEL_NAME)
                .agoraToken(TEST_AGORA_TOKEN)
                .startTime(LocalDateTime.now())
                .build();

        CallInfoDto callInfoDto = new CallInfoDto(1L, TEST_CHANNEL_NAME, TEST_AGORA_TOKEN, 1L, 2L);

        when(callRepository.findById(1L)).thenReturn(Optional.of(call));
        when(userRepository.findById(1L)).thenReturn(Optional.of(caller));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(callRepository.save(any(Call.class))).thenReturn(call);
        when(chatRoomRepository.existsByMembers(caller, receiver)).thenReturn(false);
        ChatRoom chatRoom = new ChatRoom(call, ChatRoomStatus.LIMITED);
        when(chatRoomRepository.findByCallId(any())).thenReturn(Optional.empty());
        when(chatRoomRepository.saveAndFlush(any())).thenReturn(chatRoom);
        when(chatRoomMemberRepository.saveAll(any())).thenReturn(null);

        // When: continueRelationship = true
        AgoraCallResponseDto result = agoraCallService.endCall(callInfoDto, true);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAccepted()).isTrue();
        assertThat(call.getCallStatus()).isEqualTo(CallStatus.ENDED);

        verify(callRepository).save(call);
        verify(chatRoomRepository).saveAndFlush(any());
        verify(chatRoomMemberRepository).saveAll(any());
    }

    private User createUser(Long id, String email, String phoneNumber) {
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email(email)
                .phoneNumber(phoneNumber)
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        UserProfile.builder()
                .user(user)
                .nickname("닉네임" + id)
                .age(20)
                .location("서울")
                .build();

        return user;
    }
}
