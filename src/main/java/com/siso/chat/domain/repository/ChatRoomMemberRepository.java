package com.siso.chat.domain.repository;

import com.siso.chat.domain.model.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    // 채팅방에 속한 모든 멤버 조회
    List<ChatRoomMember> findByChatRoomId(Long chatRoomId);

    // 채팅방 + 용자 조합으로 멤버 엔티티 조회
    Optional<ChatRoomMember> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
