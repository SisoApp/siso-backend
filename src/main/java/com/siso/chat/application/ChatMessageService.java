package com.siso.chat.application;

import com.siso.chat.domain.model.ChatMessage;
import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.domain.model.ChatRoomLimit;
import com.siso.chat.domain.model.ChatRoomStatus;
import com.siso.chat.domain.repository.ChatMessageRepository;
import com.siso.chat.domain.repository.ChatRoomLimitRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.chat.dto.request.ChatMessageRequestDto;
import com.siso.chat.dto.request.EditMessageRequestDto;
import com.siso.chat.dto.response.ChatMessageResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomLimitRepository chatRoomLimitRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 메시지 전송
     */
    @Transactional
    public ChatMessageResponseDto sendMessage(ChatMessageRequestDto requestDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getChatRoomId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.CHATROOM_NOT_FOUND));

        User sender = userRepository.findById(requestDto.getSenderId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // 메시지 제한 체크
        if (chatRoom.getChatRoomStatus() == ChatRoomStatus.LIMITED) {
            ChatRoomLimit limit = chatRoomLimitRepository.findByChatRoomIdAndUserId(chatRoom.getId(), sender.getId())
                    .orElseGet(() -> sender.addChatRoomLimit(chatRoom, 0)); // User 메서드로 초기화

            if (limit.getMessageCount() >= 5) {
                throw new ExpectedException(ErrorCode.MESSAGE_LIMIT_EXCEEDED);
            }

            limit.incrementMessageCount();
            chatRoomLimitRepository.save(limit);
        }

        // User 도메인 메서드로 메시지 추가
        sender.addChatMessage(chatRoom, requestDto.getContent());
        userRepository.save(sender); // cascade로 ChatMessage도 저장됨

        ChatMessage lastMessage = chatRoom.getChatMessages()
                .get(chatRoom.getChatMessages().size() - 1);

        // 채팅방의 다른 멤버들에게 알림 전송 (본인 제외)
        sendNotificationToOtherMembers(chatRoom, sender, requestDto.getContent());

        return toDto(lastMessage);
    }

    /**
     * 채팅방의 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getMessages(Long chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 메시지 수정
     */
    @Transactional
    public ChatMessageResponseDto editMessage(EditMessageRequestDto requestDto) {
        ChatMessage message = chatMessageRepository.findById(requestDto.getMessageId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(requestDto.getSenderId())) {
            throw new ExpectedException(ErrorCode.NOT_YOUR_MESSAGE);
        }

        message.updateContent(requestDto.getNewContent());
        chatMessageRepository.save(message);

        return toDto(message);
    }

    /**
     * 메시지 삭제 (soft delete)
     */
    @Transactional
    public void deleteMessage(Long messageId, Long senderId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(senderId)) {
            throw new ExpectedException(ErrorCode.NOT_YOUR_MESSAGE);
        }

        message.updateDelete(true);
        chatMessageRepository.save(message);
    }

    /**
     * 채팅방의 다른 멤버들에게 알림 전송 (본인 제외)
     */
    private void sendNotificationToOtherMembers(ChatRoom chatRoom, User sender, String messageContent) {
        try {
            String senderNickname = sender.getUserProfile() != null 
                ? sender.getUserProfile().getNickname() 
                : "익명";
            
            // 채팅방의 모든 멤버 중 발신자가 아닌 사용자들에게 알림 전송
            chatRoom.getChatRoomMembers().stream()
                .filter(member -> !member.getUser().getId().equals(sender.getId())) // 본인 제외
                .forEach(member -> {
                    try {
                        notificationService.sendMessageNotification(
                            member.getUser().getId(),
                            sender.getId(),
                            senderNickname,
                            messageContent
                        );
                    } catch (Exception e) {
                        log.warn("Failed to send notification to user {}: {}", member.getUser().getId(), e.getMessage());
                    }
                });
        } catch (Exception e) {
            log.warn("Failed to send message notifications: {}", e.getMessage());
        }
    }

    /**
     * ChatMessage → ChatMessageResponseDto 변환
     */
    private ChatMessageResponseDto toDto(ChatMessage message) {
        return new ChatMessageResponseDto(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.isDeleted()
        );
    }
}


