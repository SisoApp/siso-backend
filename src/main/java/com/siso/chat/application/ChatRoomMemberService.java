package com.siso.chat.application;

import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.domain.repository.ChatMessageRepository;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.dto.request.ChatReadRequestDto;
import com.siso.chat.dto.response.ChatRoomMemberResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomMemberService {
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    public int getUnreadCount(Long userId, Long chatRoomId) {
        // chat_room_member 테이블에서 마지막 읽음 메시지 ID 또는 timestamp 확인
        ChatRoomMember member = chatRoomMemberRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 멤버를 찾을 수 없습니다."));

        LocalDateTime lastReadAt = member.getLastReadAt(); // 마지막 읽은 시간

        // lastReadAt 이후에 생성된 메시지 수 조회
        return chatMessageRepository.countByChatRoomIdAndCreatedAtAfter(chatRoomId, lastReadAt);
    }

    /**
     * 메시지 읽음 처리
     */
    public void markAsRead(ChatReadRequestDto requestDto, User user) {
        ChatRoomMember member = chatRoomMemberRepository.findMemberByChatRoomIdAndUserId(requestDto.getChatRoomId(), user.getId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateLastReadMessageId(requestDto.getLastReadMessageId());
        chatRoomMemberRepository.save(member);
    }

    /**
     * 채팅방 멤버 목록 조회
     */
    public List<ChatRoomMemberResponseDto> getMembers(Long chatRoomId) {
        return chatRoomMemberRepository.findByChatRoomId(chatRoomId)
                .stream()
                .map(ChatRoomMemberResponseDto::fromEntity)
                .toList();
    }

    /**
     * 1대1 채팅 상대 조회
     */
    public ChatRoomMember getOtherMember(Long chatRoomId, Long myUserId) {
        return chatRoomMemberRepository.findByChatRoomId(chatRoomId).stream()
                .filter(member -> !member.getUser().getId().equals(myUserId))
                .findFirst()
                .orElseThrow(() -> new ExpectedException(ErrorCode.MEMBER_NOT_FOUND));
    }
}