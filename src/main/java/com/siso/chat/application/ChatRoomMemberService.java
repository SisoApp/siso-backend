package com.siso.chat.application;

import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.dto.request.ChatReadRequestDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomMemberService {
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public void markAsRead(ChatReadRequestDto requestDto) {
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomIdAndUserId(requestDto.getChatRoomId(), requestDto.getUserId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateLastReadMessageId(requestDto.getLastReadMessageId());
        chatRoomMemberRepository.save(member);
    }

    public List<ChatRoomMember> getMembers(Long chatRoomId) {
        return chatRoomMemberRepository.findByChatRoomId(chatRoomId);
    }
}
