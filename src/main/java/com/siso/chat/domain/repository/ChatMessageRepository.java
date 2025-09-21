package com.siso.chat.domain.repository;

import com.siso.chat.domain.model.ChatMessage;
import com.siso.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

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

    // 특정 채팅방(chatRoomId)의 메시지를 페이지 단위로 조회(채팅방 진입 시 마지막 메시지가 없는 경우 사용)
    List<ChatMessage> findByChatRoomId(Long chatRoomId, Pageable pageable);

    // 특정 채팅방(chatRoomId)에서 lastMessageId 이전 메시지 조회(스크롤 업 시 과거 메시지 불러오기 용)
    List<ChatMessage> findByChatRoomIdAndIdLessThan(Long chatRoomId, Long lastMessageId, Pageable pageable);
}
