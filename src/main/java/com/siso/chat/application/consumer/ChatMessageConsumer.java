package com.siso.chat.application.consumer;

import com.siso.chat.application.ChatRoomMemberService;
import com.siso.chat.application.event.ChatMessageEvent;
import com.siso.chat.dto.request.ChatListUpdateDto;
import com.siso.chat.infrastructure.OnlineUserRegistry;
import com.siso.common.config.RabbitMQConfig;
import com.siso.notification.application.NotificationService;
import com.siso.user.domain.model.User;
import com.siso.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 채팅 메시지 Consumer (RabbitMQ)
 * - 큐에서 메시지를 받아 WebSocket으로 전송
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomMemberService chatRoomMemberService;
    private final OnlineUserRegistry onlineUserRegistry;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE, concurrency = "3-10")
    public void handleChatMessage(ChatMessageEvent event) {
        log.info("Received chat message from queue: messageId={}, chatRoomId={}",
                event.getMessageId(), event.getChatRoomId());

        try {
            // 발신자 정보 조회
            User sender = userRepository.findById(event.getSenderId())
                    .orElse(null);

            String senderNickname = (sender != null && sender.getUserProfile() != null)
                    ? sender.getUserProfile().getNickname()
                    : "익명";

            // 수신자들에게 메시지 전송
            for (Long recipientUserId : event.getRecipientUserIds()) {
                boolean isOnline = onlineUserRegistry.isOnline(String.valueOf(recipientUserId));

                log.info("[ChatConsumer] messageId={} -> recipientUserId={} online={}",
                        event.getMessageId(), recipientUserId, isOnline);

                if (isOnline) {
                    // 온라인 사용자: WebSocket으로 실시간 전송
                    log.info("[ChatConsumer] Sending WebSocket message to userId={}", recipientUserId);
                    messagingTemplate.convertAndSendToUser(
                            String.valueOf(recipientUserId),
                            "/queue/chat-room/" + event.getChatRoomId(),
                            event.getMessage()
                    );
                } else {
                    // 오프라인 사용자: Push 알림 전송
                    log.info("[ChatConsumer] Sending push notification to userId={}", recipientUserId);
                    notificationService.sendMessageNotification(
                            recipientUserId,
                            event.getSenderId(),
                            senderNickname,
                            event.getContent()
                    );
                }

                // 채팅 목록 unread count 증가
                int unreadCount = chatRoomMemberService.getUnreadCount(recipientUserId, event.getChatRoomId());
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(recipientUserId),
                        "/queue/chat-list",
                        new ChatListUpdateDto(event.getChatRoomId(), unreadCount)
                );
            }

            log.info("Successfully delivered message: messageId={}", event.getMessageId());

        } catch (Exception e) {
            log.error("Failed to process chat message: messageId={}", event.getMessageId(), e);
            // RabbitMQ가 자동으로 재시도
            throw e;
        }
    }
}
