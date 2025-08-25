package com.siso.chat.application;

import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.domain.model.ChatRoomLimit;
import com.siso.chat.domain.repository.ChatRoomLimitRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.chat.dto.request.ChatRoomLimitRequestDto;
import com.siso.chat.dto.response.ChatRoomLimitResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomLimitService {
    private final ChatRoomLimitRepository chatRoomLimitRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    /**
     * 유저의 채팅방 메시지 제한 정보 조회
     */
    public ChatRoomLimitResponseDto getLimit(ChatRoomLimitRequestDto requestDto) {
        ChatRoom chatRoom = getChatRoom(requestDto.getChatRoomId());
        User user = getUser(requestDto.getUserId());

        ChatRoomLimit limit = getOrCreateChatRoomLimit(chatRoom, user);

        return toDto(limit);
    }

    /**
     * 메시지 전송 횟수 1 증가
     */
    @Transactional
    public ChatRoomLimitResponseDto incrementMessageCount(ChatRoomLimitRequestDto requestDto) {
        ChatRoom chatRoom = getChatRoom(requestDto.getChatRoomId());
        User user = getUser(requestDto.getUserId());

        ChatRoomLimit limit = chatRoomLimitRepository.findByChatRoomIdAndUserId(chatRoom.getId(), user.getId())
                .orElseGet(() -> user.addChatRoomLimit(chatRoom, 0)); // User 메서드 활용

        limit.incrementMessageCount();
        chatRoomLimitRepository.save(limit);

        return toDto(limit);
    }

    /**
     * 메시지 제한 초기화
     */
    @Transactional
    public void resetLimit(ChatRoomLimitRequestDto requestDto) {
        ChatRoom chatRoom = getChatRoom(requestDto.getChatRoomId());
        User user = getUser(requestDto.getUserId());

        ChatRoomLimit limit = chatRoomLimitRepository.findByChatRoomIdAndUserId(chatRoom.getId(), user.getId())
                .orElseGet(() -> user.addChatRoomLimit(chatRoom, 0));

        limit.resetMessageCount();
        chatRoomLimitRepository.save(limit);
    }

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.CHATROOM_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    private ChatRoomLimit getOrCreateChatRoomLimit(ChatRoom chatRoom, User user) {
        return chatRoomLimitRepository.findByChatRoomIdAndUserId(chatRoom.getId(), user.getId())
                .orElseGet(() -> ChatRoomLimit.builder()
                        .chatRoom(chatRoom)
                        .user(user)
                        .messageCount(0)
                        .build());
    }

    private ChatRoomLimitResponseDto toDto(ChatRoomLimit limit) {
        return new ChatRoomLimitResponseDto(
                limit.getChatRoom().getId(),
                limit.getUser().getId(),
                limit.getMessageCount()
        );
    }
}