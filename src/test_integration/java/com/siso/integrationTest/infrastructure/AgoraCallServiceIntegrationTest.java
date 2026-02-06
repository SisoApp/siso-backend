package com.siso.integrationTest.infrastructure;

import com.siso.call.application.AgoraCallService;
import com.siso.call.application.AgoraChannelNameService;
import com.siso.call.application.AgoraTokenService;
import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.domain.repository.CallRepository;
import com.siso.call.dto.CallInfoDto;
import com.siso.call.dto.request.CallRequestDto;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.integrationTest.config.IntegrationTestBase;
import com.siso.notification.application.NotificationService;
import com.siso.notification.domain.model.Notification;
import com.siso.user.domain.model.*;
import com.siso.user.domain.repository.UserProfileRepository;
import com.siso.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
class AgoraCallServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AgoraCallService agoraCallService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private CallRepository callRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @MockitoBean
    private AgoraTokenService agoraTokenService;

    @MockitoBean
    private AgoraChannelNameService agoraChannelNameService;

    @MockitoBean
    private NotificationService notificationService;

    private User caller;
    private User receiver;

    private static final String TEST_CHANNEL_NAME = "test-channel-123";
    private static final String TEST_AGORA_TOKEN = "test-agora-token";

    @BeforeEach
    void setUp() {
        caller = User.builder()
                .provider(Provider.KAKAO)
                .email("caller@test.com")
                .phoneNumber("010-1111-2222")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        receiver = User.builder()
                .provider(Provider.KAKAO)
                .email("receiver@test.com")
                .phoneNumber("010-3333-4444")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        caller = userRepository.save(caller);
        receiver = userRepository.save(receiver);

        // UserProfileDto.from()에서 NPE 방지를 위해 프로필 생성
        userProfileRepository.save(UserProfile.builder()
                .user(caller).nickname("발신자").age(25)
                .sex(Sex.MALE).preferenceSex(PreferenceSex.FEMALE)
                .mbti(Mbti.ENFP).location("서울").introduce("테스트").build());
        userProfileRepository.save(UserProfile.builder()
                .user(receiver).nickname("수신자").age(24)
                .sex(Sex.FEMALE).preferenceSex(PreferenceSex.MALE)
                .mbti(Mbti.INTJ).location("서울").introduce("테스트").build());

        when(agoraChannelNameService.generateChannelName(any(), any())).thenReturn(TEST_CHANNEL_NAME);
        when(agoraTokenService.generateToken(anyString())).thenReturn(TEST_AGORA_TOKEN);
        when(notificationService.sendCallNotification(any(), any(), any(), any(), any(), any(), any())).thenReturn(mock(Notification.class));
        when(notificationService.sendCallAcceptedNotification(any(), any(), any(), any())).thenReturn(mock(Notification.class));
        when(notificationService.sendCallDeniedNotification(any(), any())).thenReturn(mock(Notification.class));
        when(notificationService.sendCallCanceledNotification(any(), any(), any())).thenReturn(mock(Notification.class));

        // when() 설정 시 발생한 호출 카운트 초기화
        clearInvocations(agoraChannelNameService, agoraTokenService, notificationService);
    }

    @Test
    @DisplayName("통합 테스트: 통화 요청 → 수락 → 종료 플로우")
    void fullCallFlow_requestToAcceptToEnd_shouldWorkCorrectly() {
        CallRequestDto request = new CallRequestDto(receiver.getId());
        CallInfoDto callInfo = agoraCallService.requestCall(caller, request);

        assertThat(callInfo).isNotNull();
        assertThat(callInfo.getChannelName()).isEqualTo(TEST_CHANNEL_NAME);

        Call savedCall = callRepository.findById(callInfo.getId()).orElseThrow();
        assertThat(savedCall.getCallStatus()).isEqualTo(CallStatus.REQUESTED);
        assertThat(savedCall.getCaller().getId()).isEqualTo(caller.getId());
        assertThat(savedCall.getReceiver().getId()).isEqualTo(receiver.getId());

        agoraCallService.acceptCall(callInfo);

        Call acceptedCall = callRepository.findById(callInfo.getId()).orElseThrow();
        assertThat(acceptedCall.getCallStatus()).isEqualTo(CallStatus.ACCEPT);

        agoraCallService.endCall(callInfo, false);

        Call endedCall = callRepository.findById(callInfo.getId()).orElseThrow();
        assertThat(endedCall.getCallStatus()).isEqualTo(CallStatus.ENDED);
        assertThat(endedCall.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("통합 테스트: 수신자가 이미 통화 중일 때 요청 실패")
    void requestCall_whenReceiverInCall_shouldThrowException() {
        receiver.updatePresenceStatus(PresenceStatus.IN_CALL);
        userRepository.save(receiver);

        CallRequestDto request = new CallRequestDto(receiver.getId());

        assertThatThrownBy(() -> agoraCallService.requestCall(caller, request))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_IN_CALL);

        assertThat(callRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("통합 테스트: 통화 거절 플로우")
    void denyCall_shouldUpdateStatusToDeny() {
        CallRequestDto request = new CallRequestDto(receiver.getId());
        CallInfoDto callInfo = agoraCallService.requestCall(caller, request);

        agoraCallService.denyCall(callInfo);

        Call deniedCall = callRepository.findById(callInfo.getId()).orElseThrow();
        assertThat(deniedCall.getCallStatus()).isEqualTo(CallStatus.DENY);
        assertThat(deniedCall.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("통합 테스트: 통화 취소 플로우")
    void cancelCall_shouldUpdateStatusToCanceled() {
        CallRequestDto request = new CallRequestDto(receiver.getId());
        CallInfoDto callInfo = agoraCallService.requestCall(caller, request);

        agoraCallService.cancelCall(caller, callInfo.getId());

        Call canceledCall = callRepository.findById(callInfo.getId()).orElseThrow();
        assertThat(canceledCall.getCallStatus()).isEqualTo(CallStatus.CANCELED);
        assertThat(canceledCall.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("통합 테스트: 통화 종료 시 채팅방 생성")
    void endCall_withContinueRelationship_shouldCreateChatRoom() {
        CallRequestDto request = new CallRequestDto(receiver.getId());
        CallInfoDto callInfo = agoraCallService.requestCall(caller, request);
        agoraCallService.acceptCall(callInfo);

        agoraCallService.endCall(callInfo, true);

        boolean chatRoomExists = chatRoomRepository.existsByMembers(caller, receiver);
        assertThat(chatRoomExists).isTrue();
    }

    @Test
    @DisplayName("통합 테스트: 수신자가 통화를 취소하려고 하면 실패")
    void cancelCall_whenReceiverTriesToCancel_shouldThrowException() {
        CallRequestDto request = new CallRequestDto(receiver.getId());
        CallInfoDto callInfo = agoraCallService.requestCall(caller, request);

        assertThatThrownBy(() -> agoraCallService.cancelCall(receiver, callInfo.getId()))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("통합 테스트: 여러 통화 생성 및 조회")
    void multipleCallsScenario() {
        User user3 = User.builder()
                .provider(Provider.APPLE)
                .email("user3@test.com")
                .phoneNumber("010-5555-6666")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        user3 = userRepository.save(user3);

        userProfileRepository.save(UserProfile.builder()
                .user(user3).nickname("유저3").age(26)
                .sex(Sex.MALE).preferenceSex(PreferenceSex.FEMALE)
                .mbti(Mbti.ISTP).location("부산").introduce("테스트").build());

        CallRequestDto request1 = new CallRequestDto(receiver.getId());
        CallInfoDto call1 = agoraCallService.requestCall(caller, request1);

        CallRequestDto request2 = new CallRequestDto(user3.getId());
        CallInfoDto call2 = agoraCallService.requestCall(caller, request2);

        assertThat(callRepository.findById(call1.getId())).isPresent();
        assertThat(callRepository.findById(call2.getId())).isPresent();

        verify(notificationService, times(2))
                .sendCallNotification(any(), any(), any(), any(), any(), any(), any());
    }
}
