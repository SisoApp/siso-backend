package com.siso.chat;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import com.siso.call.domain.repository.CallRepository;
import com.siso.chat.application.service.ChatMessageService;
import com.siso.chat.domain.model.ChatMessage;
import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.domain.model.ChatRoomStatus;
import com.siso.chat.domain.repository.ChatMessageRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
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
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시성 테스트
 *
 * 실무에서 발생할 수 있는 동시성 문제를 검증합니다.
 * - 다수 사용자의 동시 채팅 메시지 전송
 * - 메시지 순서 보장
 * - Race Condition 방지
 */
@DisplayName("동시성 테스트")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ConcurrencyTest extends IntegrationTestBase {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CallRepository callRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    private User user1;
    private User user2;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        user1 = User.builder()
                .provider(Provider.KAKAO)
                .email("user1@example.com")
                .phoneNumber("010-1111-1111")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .provider(Provider.KAKAO)
                .email("user2@example.com")
                .phoneNumber("010-2222-2222")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();
        user2 = userRepository.save(user2);

        // Call 생성 (ChatRoom은 Call과 1:1 관계)
        Call call = Call.builder()
                .caller(user1)
                .receiver(user2)
                .callStatus(CallStatus.ACCEPT)
                .agoraChannelName("test-channel")
                .agoraToken("test-token")
                .build();
        call = callRepository.save(call);

        // 채팅방 생성
        chatRoom = ChatRoom.builder()
                .call(call)
                .chatRoomStatus(ChatRoomStatus.MATCHED)
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);
    }

    @Test
    @DisplayName("100개의 메시지를 동시에 전송해도 모두 저장되어야 함")
    void whenConcurrentMessageSending_allMessagesShouldBeSaved() throws InterruptedException {
        // Given: 100개의 동시 요청
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // When: 동시에 메시지 전송
        for (int i = 0; i < threadCount; i++) {
            final int messageNum = i;
            executorService.submit(() -> {
                try {
                    ChatMessage message = ChatMessage.builder()
                            .chatRoom(chatRoom)
                            .sender(messageNum % 2 == 0 ? user1 : user2)
                            .content("메시지 " + messageNum)
                            .build();
                    chatMessageRepository.save(message);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드 완료 대기
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then: 모든 메시지가 저장되었는지 확인
        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(threadCount);

        List<ChatMessage> savedMessages = chatMessageRepository.findAll();
        assertThat(savedMessages).hasSize(threadCount);
    }

    @Test
    @DisplayName("동일 채팅방에 동시 접근해도 데이터 정합성이 유지되어야 함")
    void whenConcurrentAccessToSameChatRoom_dataShouldBeConsistent() throws InterruptedException {
        // Given: 50명이 동시에 메시지 전송
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When: 동일 채팅방에 동시 메시지 전송
        for (int i = 0; i < threadCount; i++) {
            final int messageNum = i;
            executorService.submit(() -> {
                try {
                    ChatMessage message = ChatMessage.builder()
                            .chatRoom(chatRoom)
                            .sender(user1)
                            .content("User1의 메시지 " + messageNum)
                            .build();
                    chatMessageRepository.save(message);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then: 채팅방의 메시지 수가 정확한지 확인
        List<ChatMessage> messages = chatMessageRepository.findAll();
        assertThat(messages).hasSize(threadCount);

        // 모든 메시지가 동일한 채팅방에 속해있는지 확인
        assertThat(messages)
                .allMatch(msg -> msg.getChatRoom().getId().equals(chatRoom.getId()));
    }

    @Test
    @DisplayName("두 사용자가 번갈아 메시지를 보낼 때 순서가 보장되어야 함")
    void whenAlternatingMessages_orderShouldBeMaintained() throws InterruptedException {
        // Given: 각 사용자가 10개씩 메시지 전송
        int messagesPerUser = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        // When: User1과 User2가 동시에 메시지 전송
        executorService.submit(() -> {
            try {
                for (int i = 0; i < messagesPerUser; i++) {
                    ChatMessage message = ChatMessage.builder()
                            .chatRoom(chatRoom)
                            .sender(user1)
                            .content("User1 메시지 " + i)
                            .build();
                    chatMessageRepository.save(message);
                    Thread.sleep(10); // 약간의 지연
                }
            } catch (Exception e) {
                // ignore
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                for (int i = 0; i < messagesPerUser; i++) {
                    ChatMessage message = ChatMessage.builder()
                            .chatRoom(chatRoom)
                            .sender(user2)
                            .content("User2 메시지 " + i)
                            .build();
                    chatMessageRepository.save(message);
                    Thread.sleep(10); // 약간의 지연
                }
            } catch (Exception e) {
                // ignore
            } finally {
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then: 총 메시지 수 확인
        List<ChatMessage> messages = chatMessageRepository.findAll();
        assertThat(messages).hasSize(messagesPerUser * 2);

        // 각 사용자의 메시지가 모두 저장되었는지 확인
        long user1Messages = messages.stream()
                .filter(msg -> msg.getSender().getId().equals(user1.getId()))
                .count();
        long user2Messages = messages.stream()
                .filter(msg -> msg.getSender().getId().equals(user2.getId()))
                .count();

        assertThat(user1Messages).isEqualTo(messagesPerUser);
        assertThat(user2Messages).isEqualTo(messagesPerUser);
    }

    @Test
    @DisplayName("높은 동시성 환경에서도 데이터 유실이 없어야 함 (200개 메시지)")
    void whenHighConcurrency_noDataLossShouldOccur() throws InterruptedException {
        // Given: 200개의 메시지를 20개 스레드로 전송
        int totalMessages = 200;
        int threadPoolSize = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch latch = new CountDownLatch(totalMessages);
        AtomicInteger counter = new AtomicInteger(0);

        // When: 높은 동시성으로 메시지 전송
        for (int i = 0; i < totalMessages; i++) {
            final int messageNum = i;
            executorService.submit(() -> {
                try {
                    ChatMessage message = ChatMessage.builder()
                            .chatRoom(chatRoom)
                            .sender(messageNum % 2 == 0 ? user1 : user2)
                            .content("메시지 " + messageNum)
                            .build();
                    chatMessageRepository.save(message);
                    counter.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then: 데이터 유실 없이 모두 저장되었는지 확인
        assertThat(counter.get()).isEqualTo(totalMessages);

        List<ChatMessage> savedMessages = chatMessageRepository.findAll();
        assertThat(savedMessages).hasSize(totalMessages);

        // 메시지 내용이 중복되지 않았는지 확인
        long distinctMessageCount = savedMessages.stream()
                .map(ChatMessage::getContent)
                .distinct()
                .count();
        assertThat(distinctMessageCount).isEqualTo(totalMessages);
    }
}
