package com.siso.chat.presentation;

import com.siso.chat.application.ChatMessageService;
import com.siso.chat.application.ChatRoomMemberService;
import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.dto.request.ChatMessageRequestDto;
import com.siso.chat.dto.request.ChatReadRequestDto;
import com.siso.chat.dto.response.ChatMessageResponseDto;
import com.siso.chat.dto.response.ChatRoomMemberResponseDto;
import com.siso.chat.infrastructure.OnlineUserRegistry;
import com.siso.common.web.CurrentUser;
import com.siso.notification.application.NotificationService;
import com.siso.user.domain.model.User;
import com.siso.user.infrastructure.authentication.AccountAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final NotificationService notificationService;
    private final ChatRoomMemberService chatRoomMemberService;
    private final OnlineUserRegistry onlineUserRegistry;

    /**
     * 채팅 메시지 전송
     * 클라이언트 → /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage") // /app/chat.sendMessage
    public void sendMessage(@Payload ChatMessageRequestDto requestDto,
                            Principal principal) {
        AccountAdapter account = (AccountAdapter) ((Authentication) principal).getPrincipal();
        User sender = account.getUser();
        // 1. 메시지 저장 및 제한 처리
        ChatMessageResponseDto savedMessage = chatMessageService.sendMessage(requestDto, sender);

        // 2. 채팅방 멤버에게 실시간 전송 (본인 제외)
        List<ChatRoomMemberResponseDto> members = chatRoomMemberService.getMembers(requestDto.getChatRoomId());
        for (ChatRoomMemberResponseDto member : members) {
            if (!member.userId().equals(sender.getId())) {
                if (onlineUserRegistry.isOnline(String.valueOf(member.userId()))) {
                    // 2-1. 수신자가 온라인이면 WebSocket 전송
                    messagingTemplate.convertAndSendToUser(
                            String.valueOf(member.userId()),
                            "/queue/messages",
                            savedMessage
                    );
                } else {
                    // 2-2. 오프라인이면 NotificationService를 통해 알림 발송
                    notificationService.sendMessageNotification(
                            member.userId(),          // 수신자
                            sender.getId(),           // 발신자
                            sender.getUserProfile() != null ? sender.getUserProfile().getNickname() : "익명",
                            savedMessage.getContent() // 메시지 내용
                    );
                }
            }
        }
    }

    /**
     * 메시지 읽음 처리
     */
    @MessageMapping("/chat.readMessage") // /app/chat.readMessage
    public void readMessage(@Payload ChatReadRequestDto requestDto,
                            Principal principal) {
        AccountAdapter account = (AccountAdapter) ((Authentication) principal).getPrincipal();
        User user = account.getUser();

        // 1. 읽음 처리
        chatRoomMemberService.markAsRead(requestDto, user);

        // 2. 1대1 채팅 상대방 조회
        ChatRoomMember otherMember = chatRoomMemberService.getOtherMember(requestDto.getChatRoomId(), user.getId());

        // 3. 상대방이 온라인이면 읽음 알림 전송
        if (onlineUserRegistry.isOnline(String.valueOf(otherMember.getUser().getId()))) {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(otherMember.getUser().getId()),
                    "/queue/read-receipts",
                    requestDto
            );
        }
    }
}
