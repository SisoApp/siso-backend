package com.siso.chat.application;

import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.dto.request.ChatReadRequestDto;
import com.siso.chat.dto.response.ChatRoomMemberResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomMemberService {
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public void markAsRead(ChatReadRequestDto requestDto, User user) {
        ChatRoomMember member = chatRoomMemberRepository.findMemberByChatRoomIdAndUserId(
                        requestDto.getChatRoomId(),
                        user.getId()  // @CurrentUser로 주입된 user 사용
                )
                .orElseThrow(() -> new ExpectedException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateLastReadMessageId(requestDto.getLastReadMessageId());
        chatRoomMemberRepository.save(member);
    }

    public List<ChatRoomMemberResponseDto> getMembers(Long chatRoomId) {
        return chatRoomMemberRepository.findByChatRoomId(chatRoomId)
                .stream()
                .map(ChatRoomMemberResponseDto::fromEntity)
                .toList();
    }

    // 1대1 채팅에서 상대방 멤버 조회
    public ChatRoomMember getOtherMember(Long chatRoomId, Long myUserId) {
        return chatRoomMemberRepository.findByChatRoomId(chatRoomId).stream()
                .filter(member -> !member.getUser().getId().equals(myUserId))
                .findFirst()
                .orElseThrow(() -> new ExpectedException(ErrorCode.MEMBER_NOT_FOUND));
    }
}