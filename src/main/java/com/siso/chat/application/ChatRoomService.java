package com.siso.chat.application;

import com.siso.chat.domain.model.*;
import com.siso.chat.domain.repository.ChatMessageRepository;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.chat.dto.request.ChatRoomRequestDto;
import com.siso.chat.dto.response.ChatRoomResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public List<ChatRoomResponseDto> getChatRoomsForUser(User user) {
        Long userId = user.getId();
        log.info("getChatRoomsForUser() called for userId={}", userId);

        return chatRoomRepository.findRoomsByUserId(userId)
                .stream()
                .map(chatRoom -> {
                    log.info("Processing chatRoomId={}", chatRoom.getId());

                    // ✅ 반드시 다른 멤버가 존재해야 하므로 getOtherMember 사용
                    ChatRoomMember otherMember = getOtherMember(chatRoom, userId);
                    log.info("OtherMember for chatRoomId {} = {}", chatRoom.getId(), otherMember.getUser().getId());

                    // ✅ 메시지가 없을 수 있으므로 안전하게 조회
                    ChatMessage lastMessage = getLastMessageSafely(chatRoom);
                    log.info("LastMessage for chatRoomId {} = {}", chatRoom.getId(),
                            lastMessage != null ? lastMessage.getContent() : "NULL");

                    int unreadCount = chatRoom.getChatRoomMembers().stream()
                            .filter(m -> m.getUser().getId().equals(userId))
                            .mapToInt(m -> lastMessage != null
                                    && m.getLastReadMessageId() != null
                                    && lastMessage.getId() > m.getLastReadMessageId() ? 1 : 0)
                            .sum();
                    log.info("UnreadCount for chatRoomId {} = {}", chatRoom.getId(), unreadCount);

                    // ✅ 프로필 이미지 (무조건 otherMember 존재하므로 바로 접근 가능)
                    String profileImagePath = otherMember.getUser().getImages().isEmpty()
                            ? ""
                            : otherMember.getUser().getImages().get(0).getPath();
                    log.info("ProfileImagePath for chatRoomId {} = {}", chatRoom.getId(), profileImagePath);

                    // ✅ 닉네임 (무조건 존재한다고 가정)
                    String nickname = otherMember.getUser().getUserProfile().getNickname();
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

    private ChatMessage getLastMessageSafely(ChatRoom chatRoom) {
        return chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                .orElse(null); // ✅ 메시지 없으면 null
    }

    private void checkUserIsMember(ChatRoom chatRoom, Long userId) {
        boolean isMember = chatRoom.getChatRoomMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(userId));
        if (!isMember) {
            throw new ExpectedException(ErrorCode.ACCESS_DENIED);
        }
    }
}