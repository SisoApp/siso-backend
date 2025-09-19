package com.siso.chat.domain.repository;

import com.siso.chat.domain.model.ChatMessage;
import com.siso.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>  {
    // 채팅방의 메시지를 보낸 시간 기준으로 정렬하여 조회
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    // 가장 최근 메시지 하나만 가져오기
    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    // 특정 채팅방에서 lastReadAt 이후 생성된 메시지 개수 조회
    int countByChatRoomIdAndCreatedAtAfter(Long chatRoomId, LocalDateTime createdAt);
}
