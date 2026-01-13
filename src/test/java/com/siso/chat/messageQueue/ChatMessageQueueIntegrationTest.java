package com.siso.chat.messageQueue;

import com.siso.chat.application.event.ChatMessageEvent;
import com.siso.chat.application.publisher.ChatMessagePublisher;
import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.domain.model.ChatRoomStatus;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.chat.dto.response.ChatMessageResponseDto;
import com.siso.config.IntegrationTestBase;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 채팅 메시지 큐 통합 테스트
 */
@DisplayName("채팅 메시지 큐 통합 테스트")
class ChatMessageQueueIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ChatMessagePublisher chatMessagePublisher;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private User sender;
    private User recipient;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        // Given: 발신자 생성
        sender = User.builder()
                .provider(Provider.KAKAO)
                .email("sender@example.com")
                .phoneNumber("010-1111-1111")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();
        sender = userRepository.save(sender);

        // Given: 수신자 생성
        recipient = User.builder()
                .provider(Provider.KAKAO)
                .email("recipient@example.com")
                .phoneNumber("010-2222-2222")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();
        recipient = userRepository.save(recipient);

        // Given: 채팅방 생성
        chatRoom = ChatRoom.builder()
                .chatRoomStatus(ChatRoomStatus.MATCHED)
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);
    }

    @Test
    @DisplayName("채팅 메시지를 RabbitMQ에 발행")
    void publishMessage_shouldSendToQueue() {
        // Given: 채팅 메시지 DTO
        ChatMessageResponseDto message = new ChatMessageResponseDto(
                1L,
                chatRoom.getId(),
                sender.getId(),
                "안녕하세요!",
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
        );

        List<Long> recipients = List.of(recipient.getId());

        // When: RabbitMQ에 발행
        ChatMessageEvent event = ChatMessageEvent.from(message, recipients);
        chatMessagePublisher.publishMessage(event);

        // Then: 메시지가 큐에 발행됨 (로그 확인)
        assertThat(event.getMessageId()).isEqualTo(1L);
        assertThat(event.getChatRoomId()).isEqualTo(chatRoom.getId());
        assertThat(event.getRecipientUserIds()).hasSize(1);
        assertThat(event.getRecipientUserIds()).contains(recipient.getId());
    }

    @Test
    @DisplayName("메시지 이벤트 생성 - from() 메서드")
    void createEvent_shouldContainAllData() {
        // Given: 메시지 DTO
        ChatMessageResponseDto message = new ChatMessageResponseDto(
                100L,
                chatRoom.getId(),
                sender.getId(),
                "테스트 메시지",
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
        );

        List<Long> recipients = List.of(recipient.getId());

        // When: 이벤트 생성
        ChatMessageEvent event = ChatMessageEvent.from(message, recipients);

        // Then: 모든 데이터 검증
        assertThat(event.getMessageId()).isEqualTo(100L);
        assertThat(event.getChatRoomId()).isEqualTo(chatRoom.getId());
        assertThat(event.getSenderId()).isEqualTo(sender.getId());
        assertThat(event.getContent()).isEqualTo("테스트 메시지");
        assertThat(event.getRecipientUserIds()).containsExactly(recipient.getId());
        assertThat(event.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("여러 수신자에게 메시지 발행")
    void publishMessage_withMultipleRecipients() {
        // Given: 여러 수신자
        User recipient2 = User.builder()
                .provider(Provider.KAKAO)
                .email("recipient2@example.com")
                .phoneNumber("010-3333-3333")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();
        recipient2 = userRepository.save(recipient2);

        ChatMessageResponseDto message = new ChatMessageResponseDto(
                2L,
                chatRoom.getId(),
                sender.getId(),
                "여러 명에게 전송",
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
        );

        List<Long> recipients = List.of(recipient.getId(), recipient2.getId());

        // When: 발행
        ChatMessageEvent event = ChatMessageEvent.from(message, recipients);
        chatMessagePublisher.publishMessage(event);

        // Then: 수신자 목록 검증
        assertThat(event.getRecipientUserIds()).hasSize(2);
        assertThat(event.getRecipientUserIds()).containsExactlyInAnyOrder(
                recipient.getId(),
                recipient2.getId()
        );
    }
}
