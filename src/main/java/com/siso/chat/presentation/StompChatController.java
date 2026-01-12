package com.siso.chat.presentation;

import com.siso.chat.application.ChatMessageService;
import com.siso.chat.application.ChatRoomMemberService;
import com.siso.chat.application.event.ChatMessageEvent;
import com.siso.chat.application.publisher.ChatMessagePublisher;
import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.dto.request.ChatListUpdateDto;
import com.siso.chat.dto.request.ChatMessageRequestDto;
import com.siso.chat.dto.request.ChatReadRequestDto;
import com.siso.chat.dto.response.ChatMessageResponseDto;
import com.siso.chat.dto.response.ChatRoomMemberResponseDto;
import com.siso.chat.infrastructure.OnlineUserRegistry;
import com.siso.notification.application.NotificationService;
import com.siso.user.domain.model.User;
import com.siso.user.infrastructure.authentication.AccountAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatMessagePublisher chatMessagePublisher;  // 추가: RabbitMQ Publisher
    private final NotificationService notificationService;
    private final ChatRoomMemberService chatRoomMemberService;
    private final OnlineUserRegistry onlineUserRegistry;

    /**
     * 채팅 메시지 전송 (메시지 큐 방식)
     * 클라이언트 → /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage") // /app/chat.sendMessage
    public void sendMessage(@Payload ChatMessageRequestDto requestDto,
                            Principal principal) {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        AccountAdapter account = (AccountAdapter) auth.getPrincipal();
        User sender = account.getUser();

        log.info("[sendMessage] chatRoomId={}, senderId={}", requestDto.getChatRoomId(), sender.getId());

        // 1. 메시지 저장 및 제한 처리 (DB에 저장)
        ChatMessageResponseDto savedMessage = chatMessageService.sendMessage(requestDto, sender);

        // 2. 수신자 목록 조회 (본인 제외)
        List<ChatRoomMemberResponseDto> members = chatRoomMemberService.getMembers(requestDto.getChatRoomId());
        List<Long> recipientUserIds = members.stream()
                .map(ChatRoomMemberResponseDto::userId)
                .filter(userId -> !userId.equals(sender.getId()))
                .collect(Collectors.toList());

        // 3. RabbitMQ에 이벤트 발행 (비동기)
        ChatMessageEvent event = ChatMessageEvent.from(savedMessage, recipientUserIds);
        chatMessagePublisher.publishMessage(event);

        log.info("[sendMessage] Published to RabbitMQ: messageId={}, recipients={}",
                savedMessage.getId(), recipientUserIds.size());
    }

    /**
     * 메시지 읽음 처리
     */
    @MessageMapping("/chat.readMessage") // /app/chat.readMessage
    public void readMessage(@Payload ChatReadRequestDto requestDto,
                            Principal principal) {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        AccountAdapter account = (AccountAdapter) auth.getPrincipal();
        User user = account.getUser();

        // 1. 읽음 처리
        chatRoomMemberService.markAsRead(requestDto, user);

        // 2. 1대1 채팅 상대방 조회
        ChatRoomMember otherMember = chatRoomMemberService.getOtherMember(requestDto.getChatRoomId(), user.getId());
        boolean isOnline = onlineUserRegistry.isOnline(String.valueOf(otherMember.getUser().getId()));
        log.info("[readMessage] readerId={} -> otherMember userId={} online={} | 현재 onlineUsers={}", user.getId(), otherMember.getUser().getId(), isOnline, onlineUserRegistry.getOnlineUsers().keySet());

        // 3. 상대방이 온라인이면 읽음 알림 전송
        if (isOnline) {
            log.info("[readMessage] OtherMember userId={} online={} -> Sending WS read receipt",
                    otherMember.getUser().getId(), isOnline);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(otherMember.getUser().getId()),
                    "/queue/read-receipt/" + requestDto.getChatRoomId(),
                    requestDto
            );
        } else {
            log.info("[readMessage] OtherMember userId={} online={} -> Skipping WS, offline user",
                    otherMember.getUser().getId(), isOnline);
        }

        // unread count 감소
        int unreadCount = chatRoomMemberService.getUnreadCount(user.getId(), requestDto.getChatRoomId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(user.getId()),
                "/queue/chat-list",
                new ChatListUpdateDto(requestDto.getChatRoomId(), unreadCount)
        );
    }
}
