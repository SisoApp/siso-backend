package com.siso.chat.application;

import com.siso.chat.domain.model.*;
import com.siso.chat.domain.repository.ChatMessageRepository;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.chat.dto.request.ChatMessageRequestDto;
import com.siso.chat.dto.request.EditMessageRequestDto;
import com.siso.chat.dto.response.ChatMessageResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final NotificationService notificationService;

    /**
     * 메시지 전송
     */
    @Transactional
    public ChatMessageResponseDto sendMessage(ChatMessageRequestDto requestDto, User sender) {
        log.info("[sendMessage] chatRoomId={}, senderId={}, content={}",
                requestDto.getChatRoomId(), sender.getId(), requestDto.getContent());

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getChatRoomId())
                .orElseThrow(() -> {
                    log.error("CHATROOM_NOT_FOUND for chatRoomId={}", requestDto.getChatRoomId());
                    return new ExpectedException(ErrorCode.CHATROOM_NOT_FOUND);
                });

        // 메시지 제한 체크 (LIMITED 상태일 때만 적용)
        if (chatRoom.getChatRoomStatus() == ChatRoomStatus.LIMITED) {
            log.info("ChatRoom is LIMITED. Checking message limit for senderId={}", sender.getId());
            ChatRoomMember member = chatRoomMemberRepository.findMemberByChatRoomIdAndUserId(chatRoom.getId(), sender.getId())
                    .orElseThrow(() -> {
                        log.error("MEMBER_NOT_FOUND for chatRoomId={}, senderId={}", chatRoom.getId(), sender.getId());
                        return new ExpectedException(ErrorCode.MEMBER_NOT_FOUND);
                    });

            if (!member.canSendMessage()) {
                log.warn("MESSAGE_LIMIT_EXCEEDED for senderId={}, chatRoomId={}", sender.getId(), chatRoom.getId());
                throw new ExpectedException(ErrorCode.MESSAGE_LIMIT_EXCEEDED);
            }

            member.increaseMessageCount();
            chatRoomMemberRepository.save(member);
            log.info("Increased message count for memberId={}", member.getId());
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content(requestDto.getContent())
                .build();

        log.info("Saving ChatMessage...");
        ChatMessage saved = chatMessageRepository.save(message);
        log.info("Saved ChatMessage: id={}, content={}", saved.getId(), saved.getContent());

        return toDto(saved);
    }

    /**
     * 채팅방의 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getMessages(Long chatRoomId, Long lastMessageId, int size) {
        log.info("Fetching messages for chatRoomId={} lastMessageId={} size={}", chatRoomId, lastMessageId, size);

        List<ChatMessage> messages;
        if (lastMessageId == null) {
            // 채팅방 진입 시 → 최신 메시지 30개
            Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
            messages = chatMessageRepository.findByChatRoomId(chatRoomId, pageable);
        } else {
            // 과거 메시지 요청 → lastMessageId 이전 메시지
            Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
            messages = chatMessageRepository.findByChatRoomIdAndIdLessThan(chatRoomId, lastMessageId, pageable);
        }

        // DTO로 변환 + 오래된 순서로 정렬
        return messages.stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(ChatMessageResponseDto::getCreatedAt))
                .toList();
    }

    /**
     * 메시지 수정
     */
    @Transactional
    public ChatMessageResponseDto editMessage(EditMessageRequestDto requestDto, User sender) {
        log.info("Editing messageId={} by senderId={}", requestDto.getMessageId(), sender.getId());
        ChatMessage message = chatMessageRepository.findById(requestDto.getMessageId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(sender.getId())) {
            log.error("NOT_YOUR_MESSAGE: messageId={}, senderId={}", message.getId(), sender.getId());
            throw new ExpectedException(ErrorCode.NOT_YOUR_MESSAGE);
        }

        message.updateContent(requestDto.getNewContent());
        chatMessageRepository.save(message);
        log.info("Updated messageId={}", message.getId());

        return toDto(message);
    }

    /**
     * 메시지 삭제 (soft delete)
     */
    @Transactional
    public void deleteMessage(Long messageId, User sender) {
        log.info("Deleting messageId={} by senderId={}", messageId, sender.getId());
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(sender.getId())) {
            log.error("NOT_YOUR_MESSAGE: messageId={}, senderId={}", message.getId(), sender.getId());
            throw new ExpectedException(ErrorCode.NOT_YOUR_MESSAGE);
        }

        message.updateDelete(true);
        chatMessageRepository.save(message);
        log.info("Soft-deleted messageId={}", message.getId());
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