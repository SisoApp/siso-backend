package com.siso.chat.domain.repository;

import com.siso.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // call.id 로 ChatRoom 찾기
    Optional<ChatRoom> findByCallId(Long callId);
    // 사용자가 속한 채팅방 조회
    List<ChatRoom> findByChatRoomMembersUserId(Long userId);
}
