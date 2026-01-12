package com.siso.chat.application.publisher;

import com.siso.chat.application.event.ChatMessageEvent;
import com.siso.common.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * 채팅 메시지 Publisher (RabbitMQ)
 * - DB에 저장 후 메시지 큐에 발행
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 채팅 메시지를 RabbitMQ에 발행
     */
    public void publishMessage(ChatMessageEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CHAT_EXCHANGE,
                    RabbitMQConfig.CHAT_ROUTING_KEY,
                    event
            );

            log.info("Published chat message to queue: messageId={}, chatRoomId={}",
                    event.getMessageId(), event.getChatRoomId());

        } catch (Exception e) {
            log.error("Failed to publish chat message: messageId={}", event.getMessageId(), e);
            // 메시지 큐 실패 시에도 DB에는 저장되어 있으므로
            // 나중에 재시도하거나 직접 WebSocket으로 전송할 수 있음
        }
    }
}
