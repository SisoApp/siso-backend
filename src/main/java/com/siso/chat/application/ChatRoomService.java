package com.siso.chat.application;

import com.siso.chat.domain.model.*;
import com.siso.chat.domain.repository.ChatRoomLimitRepository;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.chat.dto.response.ChatRoomResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomLimitRepository chatRoomLimitRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoom createChatRoom(List<Long> userIds) {
        ChatRoom chatRoom = ChatRoom.builder()
                .call(null)
                .chatRoomStatus(ChatRoomStatus.LIMITED)
                .build();

        userIds.forEach(userId -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

            // User 도메인 메서드 활용
            user.addChatRoomMember(chatRoom, null);
            user.addChatRoomLimit(chatRoom, 0);

            userRepository.save(user); // cascade 때문에 member, limit도 함께 저장됨
        });

        return chatRoomRepository.save(chatRoom);
    }

    public List<ChatRoomResponseDto> getChatRoomsForUser(Long userId) {
        // 사용자가 속한 모든 채팅방 조회
        return chatRoomRepository.findByChatRoomMembersUserId(userId)
                .stream()
                .map(chatRoom -> {
                    // 다른 멤버(1:1 기준) 조회
                    ChatRoomMember otherMember = chatRoom.getChatRoomMembers().stream()
                            .filter(m -> !m.getUser().getId().equals(userId))
                            .findFirst()
                            .orElse(null);

                    // 마지막 메시지 조회
                    ChatMessage lastMessage = chatRoom.getChatMessages().stream()
                            .max(Comparator.comparing(ChatMessage::getCreatedAt))
                            .orElse(null);

                    // 읽지 않은 메시지 개수 계산 (마지막 메시지 기준)
                    int unreadCount = chatRoom.getChatRoomMembers().stream()
                            .filter(m -> m.getUser().getId().equals(userId))
                            .mapToInt(m -> lastMessage != null
                                    && m.getLastReadMessageId() != null
                                    && lastMessage.getId() > m.getLastReadMessageId() ? 1 : 0)
                            .sum();

                    // DTO 생성
                    return new ChatRoomResponseDto(
                            chatRoom.getId(),
                            otherMember != null ? otherMember.getUser().getUserProfile().getNickname() : "",
                            otherMember != null ? otherMember.getUser().getImages().get(0).getPath() : "",  // 수정 필요
                            chatRoom.getChatRoomMembers().size(),
                            lastMessage != null ? lastMessage.getContent() : "",
                            lastMessage != null ? lastMessage.getCreatedAt() : null,
                            unreadCount
                    );
                })
                .toList();
    }


    public void leaveChatRoom(Long chatRoomId, Long userId) {
        chatRoomMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId)
                .ifPresent(chatRoomMemberRepository::delete);
    }

    public void unlockChatRoom(ChatRoom chatRoom) {
        chatRoom.updateChatRoomStatus(ChatRoomStatus.MATCHED);
        chatRoomRepository.save(chatRoom);
    }
}

