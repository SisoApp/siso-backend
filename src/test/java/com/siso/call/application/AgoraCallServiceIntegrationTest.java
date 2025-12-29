package com.siso.call.application;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.domain.repository.CallRepository;
import com.siso.call.dto.CallInfoDto;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.config.IntegrationTestBase;
import com.siso.notification.application.NotificationService;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AgoraCallService 통합 테스트
 *
 * 실제 DB와 함께 동작하는 통합 테스트
 * 외부 서비스(Agora, Notification)는 Mock 사용
 */
@DisplayName("AgoraCallService 통합 테스트")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AgoraCallServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AgoraCallService agoraCallService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CallRepository callRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @MockBean
    private AgoraTokenService agoraTokenService;

    @MockBean
    private AgoraChannelNameService agoraChannelNameService;

    @MockBean
    private NotificationService notificationService;

    private User caller;
    private User receiver;

    private static final String TEST_CHANNEL_NAME = "test-channel-123";
    private static final String TEST_AGORA_TOKEN = "test-agora-token";

    @BeforeEach
    void setUp() {
        // Given: 발신자와 수신자 생성 및 저장
        caller = User.builder()
                .provider(Provider.KAKAO)
                .email("caller@test.com")
                .phoneNumber("010-1111-2222")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();

        receiver = User.builder()
                .provider(Provider.KAKAO)
                .email("receiver@test.com")
                .phoneNumber("010-3333-4444")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();

        caller = userRepository.save(caller);
        receiver = userRepository.save(receiver);

        // Mock 설정
        when(agoraChannelNameService.generateChannelName(any(), any())).thenReturn(TEST_CHANNEL_NAME);
        when(agoraTokenService.generateToken(anyString())).thenReturn(TEST_AGORA_TOKEN);
        doNothing().when(notificationService).sendCallNotification(any(), any(), any(), any(), any(), any(), any());
        doNothing().when(notificationService).sendCallAcceptedNotification(any(), any(), any(), any());
        doNothing().when(notificationService).sendCallDeniedNotification(any(), any());
        doNothing().when(notificationService).sendCallCanceledNotification(any(), any(), any());
    }

    @Test
    @DisplayName("통합 테스트: 통화 요청 → 수락 → 종료 플로우")
    void fullCallFlow_requestToAcceptToEnd_shouldWorkCorrectly() {
        // 1. 통화 요청
        CallRequestDto request = new CallRequestDto(receiver.getId());
        CallInfoDto callInfo = agoraCallService.requestCall(caller, request);

        assertThat(callInfo).isNotNull();
        assertThat(callInfo.getAgoraChannelName()).isEqualTo(TEST_CHANNEL_NAME);

        // 2. DB에 Call이 저장되었는지 확인
        Call savedCall = callRepository.findById(callInfo.getId()).orElseThrow();
        assertThat(savedCall.getCallStatus()).isEqualTo(CallStatus.REQUESTED);
        assertThat(savedCall.getCaller().getId()).isEqualTo(caller.getId());
        assertThat(savedCall.getReceiver().getId()).isEqualTo(receiver.getId());

        // 3. 통화 수락
        agoraCallService.acceptCall(callInfo);

        // 4. 상태가 ACCEPT로 변경되었는지 확인
        Call acceptedCall = callRepository.findById(callInfo.getId()).orElseThrow();
        assertThat(acceptedCall.getCallStatus()).isEqualTo(CallStatus.ACCEPT);

        // 5. 통화 종료
        agoraCallService.endCall(callInfo, false);

        // 6. 상태가 ENDED로 변경되었는지 확인
        Call endedCall = callRepository.findById(callInfo.getId()).orElseThrow();
        assertThat(endedCall.getCallStatus()).isEqualTo(CallStatus.ENDED);
        assertThat(endedCall.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("통합 테스트: 수신자가 이미 통화 중일 때 요청 실패")
    void requestCall_whenReceiverInCall_shouldThrowException() {
        // Given: 수신자를 IN_CALL 상태로 변경
        receiver.updatePresenceStatus(PresenceStatus.IN_CALL);
        userRepository.save(receiver);

        // When & Then: 통화 요청 시 예외 발생
        CallRequestDto request = new CallRequestDto(receiver.getId());

        assertThatThrownBy(() -> agoraCallService.requestCall(caller, request))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_IN_CALL);

        // Then: Call이 DB에 저장되지 않음
        assertThat(callRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("통합 테스트: 통화 거절 플로우")
    void denyCall_shouldUpdateStatusToDeny() {
        // Given: 통화 요청
        CallRequestDto request = new CallRequestDto(receiver.getId());
        CallInfoDto callInfo = agoraCallService.requestCall(caller, request);

        // When: 통화 거절
        agoraCallService.denyCall(callInfo);

        // Then: 상태가 DENY로 변경됨
        Call deniedCall = callRepository.findById(callInfo.getId()).orElseThrow();
        assertThat(deniedCall.getCallStatus()).isEqualTo(CallStatus.DENY);
        assertThat(deniedCall.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("통합 테스트: 통화 취소 플로우")
    void cancelCall_shouldUpdateStatusToCanceled() {
        // Given: 통화 요청
        CallRequestDto request = new CallRequestDto(receiver.getId());
        CallInfoDto callInfo = agoraCallService.requestCall(caller, request);

        // When: 발신자가 취소
        agoraCallService.cancelCall(caller, callInfo.getId());

        // Then: 상태가 CANCELED로 변경됨
        Call canceledCall = callRepository.findById(callInfo.getId()).orElseThrow();
        assertThat(canceledCall.getCallStatus()).isEqualTo(CallStatus.CANCELED);
        assertThat(canceledCall.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("통합 테스트: 통화 종료 시 채팅방 생성")
    void endCall_withContinueRelationship_shouldCreateChatRoom() {
        // Given: 통화 요청 및 수락
        CallRequestDto request = new CallRequestDto(receiver.getId());
        CallInfoDto callInfo = agoraCallService.requestCall(caller, request);
        agoraCallService.acceptCall(callInfo);

        // When: 통화 종료 + 관계 이어가기
        agoraCallService.endCall(callInfo, true);

        // Then: 채팅방이 생성됨
        boolean chatRoomExists = chatRoomRepository.existsByMembers(caller, receiver);
        assertThat(chatRoomExists).isTrue();
    }

    @Test
    @DisplayName("통합 테스트: 수신자가 통화를 취소하려고 하면 실패")
    void cancelCall_whenReceiverTriesToCancel_shouldThrowException() {
        // Given: 통화 요청
        CallRequestDto request = new CallRequestDto(receiver.getId());
        CallInfoDto callInfo = agoraCallService.requestCall(caller, request);

        // When & Then: 수신자가 취소하려고 하면 예외 발생
        assertThatThrownBy(() -> agoraCallService.cancelCall(receiver, callInfo.getId()))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("통합 테스트: 여러 통화 생성 및 조회")
    void multipleCallsScenario() {
        // Given: 여러 명의 사용자
        User user3 = User.builder()
                .provider(Provider.APPLE)
                .email("user3@test.com")
                .phoneNumber("010-5555-6666")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();

        user3 = userRepository.save(user3);

        // When: 여러 통화 생성
        CallRequestDto request1 = new CallRequestDto(receiver.getId());
        CallInfoDto call1 = agoraCallService.requestCall(caller, request1);

        CallRequestDto request2 = new CallRequestDto(user3.getId());
        CallInfoDto call2 = agoraCallService.requestCall(caller, request2);

        // Then: 두 통화 모두 DB에 저장됨
        assertThat(callRepository.findById(call1.getId())).isPresent();
        assertThat(callRepository.findById(call2.getId())).isPresent();

        // Then: 알림이 각 수신자에게 전송됨
        verify(notificationService, times(2))
                .sendCallNotification(any(), any(), any(), any(), any(), any(), any());
    }
}
