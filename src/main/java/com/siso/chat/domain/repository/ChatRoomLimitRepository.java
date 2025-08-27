package com.siso.chat.domain.repository;

import com.siso.chat.domain.model.ChatRoomLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomLimitRepository extends JpaRepository<ChatRoomLimit, Long> {
    // 채팅방 + 용자 조합으로 제한 정보 조회 → 메시지 전송 횟수 확인할 때 사용
    Optional<ChatRoomLimit> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}