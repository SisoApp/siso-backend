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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public List<ChatRoomResponseDto> getChatRoomsForUser(User user) {
        Long userId = user.getId();
        log.info("getChatRoomsForUser() called for userId={}", userId);

        return chatRoomRepository.findRoomsByUserId(userId)
                .stream()
                .map(chatRoom -> {
                    log.info("Processing chatRoomId={}", chatRoom.getId());

                    // 다른 멤버 가져오기 (자기 자신 제외)
                    ChatRoomMember otherMember = chatRoom.getChatRoomMembers().stream()
                            .filter(member -> !Objects.equals(member.getUser().getId(), userId)) // 자기 자신 제외
                            .findFirst()
                            .orElse(null);

                    log.info("OtherMember for chatRoomId {} = {}", chatRoom.getId(),
                            otherMember != null ? otherMember.getUser().getId() : "NULL");
                    ChatMessage lastMessage = getLastMessage(chatRoom);
                    log.info("LastMessage for chatRoomId {} = {}", chatRoom.getId(),
                            lastMessage != null ? lastMessage.getContent() : "NULL");

                    int unreadCount = chatRoom.getChatRoomMembers().stream()
                            .filter(m -> m.getUser().getId().equals(userId))
                            .mapToInt(m -> lastMessage != null
                                    && m.getLastReadMessageId() != null
                                    && lastMessage.getId() > m.getLastReadMessageId() ? 1 : 0)
                            .sum();

                    log.info("UnreadCount for chatRoomId {} = {}", chatRoom.getId(), unreadCount);

                    // 첫 번째 이미지 경로
                    String profileImagePath = Optional.ofNullable(otherMember)
                            .map(ChatRoomMember::getUser)
                            .map(User::getImages)
                            .filter(images -> !images.isEmpty())
                            .map(images -> images.get(0).getPath())
                            .orElse("");
                    log.info("ProfileImagePath for chatRoomId {} = {}", chatRoom.getId(), profileImagePath);

                    // 닉네임
                    String nickname = Optional.ofNullable(otherMember)
                            .map(ChatRoomMember::getUser)
                            .map(User::getUserProfile)
                            .map(UserProfile::getNickname)
                            .orElse("익명");
                    log.info("Nickname for chatRoomId {} = {}", chatRoom.getId(), nickname);

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