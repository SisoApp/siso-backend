package com.siso.chat.application;

import com.siso.chat.domain.model.*;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.chat.dto.request.ChatRoomRequestDto;
import com.siso.chat.dto.response.ChatRoomResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public List<ChatRoomResponseDto> getChatRoomsForUser(User user) {
        Long userId = user.getId();

        return chatRoomRepository.findRoomsByUserId(userId)
                .stream()
                .map(chatRoom -> {
                    ChatRoomMember otherMember = getOtherMember(chatRoom, userId);
                    ChatMessage lastMessage = getLastMessage(chatRoom);

                    int unreadCount = chatRoom.getChatRoomMembers().stream()
                            .filter(m -> m.getUser().getId().equals(userId))
                            .mapToInt(m -> lastMessage != null
                                    && m.getLastReadMessageId() != null
                                    && lastMessage.getId() > m.getLastReadMessageId() ? 1 : 0)
                            .sum();

                    // 첫 번째 이미지 경로 가져오기
                    String profileImagePath = Optional.ofNullable(otherMember)
                            .map(ChatRoomMember::getUser)
                            .map(User::getImages)
                            .filter(images -> !images.isEmpty())
                            .map(images -> images.get(0).getPath())
                            .orElse("");

                    // 닉네임도 안전 처리
                    String nickname = Optional.ofNullable(otherMember)
                            .map(ChatRoomMember::getUser)
                            .map(User::getUserProfile)
                            .map(UserProfile::getNickname)
                            .orElse("익명");

                    return new ChatRoomResponseDto(
                            chatRoom.getId(),
                            nickname,
                            profileImagePath,
                            chatRoom.getChatRoomMembers().size(),
                            lastMessage != null ? lastMessage.getContent() : "",
                            lastMessage != null ? lastMessage.getCreatedAt() : null,
                            unreadCount
                    );
                })
                .toList();
    }

    public void acceptChatRoom(ChatRoomRequestDto requestDto, User user) {
        ChatRoom chatRoom = getChatRoom(requestDto.getChatRoomId());
        checkUserIsMember(chatRoom, user.getId());

        chatRoom.updateChatRoomStatus(ChatRoomStatus.MATCHED);
        chatRoom.getChatRoomMembers().forEach(ChatRoomMember::resetMessageCount);
        chatRoomRepository.save(chatRoom);
    }

    public void leaveChatRoom(ChatRoomRequestDto requestDto, User user) {
        ChatRoom chatRoom = getChatRoom(requestDto.getChatRoomId());
        ChatRoomMember member = getChatRoomMember(chatRoom.getId(), user.getId());

        member.leave();

        boolean allLeft = chatRoom.getChatRoomMembers().stream()
                .allMatch(m -> m.getChatRoomMemberStatus() == ChatRoomMemberStatus.LEFT);

        if (allLeft) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    public void unlockChatRoom(ChatRoom chatRoom) {
        chatRoomRepository.save(chatRoom);
    }

    // =================== Helper Methods ===================

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.CHATROOM_NOT_FOUND));
    }

    private ChatRoomMember getChatRoomMember(Long chatRoomId, Long userId) {
        return chatRoomMemberRepository.findMemberByChatRoomIdAndUserId(chatRoomId, userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.NOT_CHATROOM_MEMBER));
    }

    private ChatRoomMember getOtherMember(ChatRoom chatRoom, Long userId) {
        return chatRoom.getChatRoomMembers().stream()
                .filter(m -> !m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ExpectedException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private ChatMessage getLastMessage(ChatRoom chatRoom) {
        return chatRoom.getChatMessages().stream()
                .max(Comparator.comparing(ChatMessage::getCreatedAt))
                .orElseThrow(() -> new ExpectedException(ErrorCode.CHATROOM_EMPTY));
    }

    private void checkUserIsMember(ChatRoom chatRoom, Long userId) {
        boolean isMember = chatRoom.getChatRoomMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(userId));
        if (!isMember) {
            throw new ExpectedException(ErrorCode.ACCESS_DENIED);
        }
    }
}