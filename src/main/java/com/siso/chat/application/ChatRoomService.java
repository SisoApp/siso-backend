package com.siso.chat.application;

import com.siso.chat.domain.model.*;
import com.siso.chat.domain.repository.ChatRoomLimitRepository;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.chat.dto.request.ChatRoomRequestDto;
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
    private final UserRepository userRepository;

//    @Transactional
//    public ChatRoom createChatRoom(List<Long> userIds) {
//        ChatRoom chatRoom = ChatRoom.builder()
//                .call(null)
//                .chatRoomStatus(ChatRoomStatus.LIMITED)
//                .build();
//
//        userIds.forEach(userId -> {
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
//
//            // User 도메인 메서드 활용
//            user.addChatRoomMember(chatRoom, null);
//            user.addChatRoomLimit(chatRoom, 0);
//
//            userRepository.save(user); // cascade 때문에 member, limit도 함께 저장됨
//        });
//
//        return chatRoomRepository.save(chatRoom);
//    }

    public List<ChatRoomResponseDto> getChatRoomsForUser(Long userId) {
        // 사용자가 속한 모든 채팅방 조회
        return chatRoomRepository.findByChatRoomMembersUserId(userId)
                .stream()
                .map(chatRoom -> {
                    // 다른 멤버(1:1 기준) 조회
                    ChatRoomMember otherMember = chatRoom.getChatRoomMembers().stream()
                            .filter(m -> !m.getUser().getId().equals(userId))
                            .findFirst()
                            .orElseThrow(() -> new ExpectedException(ErrorCode.MEMBER_NOT_FOUND));

                    // 마지막 메시지 조회
                    ChatMessage lastMessage = chatRoom.getChatMessages().stream()
                            .max(Comparator.comparing(ChatMessage::getCreatedAt))
                            .orElseThrow(() -> new ExpectedException(ErrorCode.CHATROOM_EMPTY));

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
                            otherMember != null ? otherMember.getUser().getUserProfile().getProfileImage() : null,
                            chatRoom.getChatRoomMembers().size(),
                            lastMessage != null ? lastMessage.getContent() : "",
                            lastMessage != null ? lastMessage.getCreatedAt() : null,
                            unreadCount
                    );
                })
                .toList();
    }

    @Transactional
    public void acceptChatRoom(ChatRoomRequestDto requestDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getChatRoomId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.CHATROOM_NOT_FOUND));

        User receiver = userRepository.findById(requestDto.getReceiverId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // receiver가 멤버인지 확인
        boolean isMember = chatRoom.getChatRoomMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(receiver.getId()));

        if (!isMember) {
            throw new ExpectedException(ErrorCode.ACCESS_DENIED);
        }

        chatRoom.updateChatRoomStatus(ChatRoomStatus.MATCHED);
        chatRoomRepository.save(chatRoom);
    }

    public void leaveChatRoom(ChatRoomRequestDto requestDto, Long userId) {
        chatRoomMemberRepository.findByChatRoomIdAndUserId(requestDto.getChatRoomId(), userId)
                .ifPresent(chatRoomMemberRepository::delete);
    }

    public void unlockChatRoom(ChatRoom chatRoom) {
        chatRoomRepository.save(chatRoom);
    }
}

