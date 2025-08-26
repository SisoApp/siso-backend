package com.siso.chat.domain.repository;

import com.siso.chat.domain.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>  {
    // 채팅방의 메시지를 보낸 시간 기준으로 정렬하여 조회
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}
