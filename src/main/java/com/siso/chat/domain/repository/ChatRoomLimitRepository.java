package com.siso.chat.domain.repository;

import com.siso.chat.domain.model.ChatRoomLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomLimitRepository extends JpaRepository<ChatRoomLimit, Long> {
    // 채팅방 멤버로 채팅방 제한 관리 조회
    Optional<ChatRoomLimit> findByChatRoomMemberId(Long chatRoomMemberId);

    // 채팅방 + 용자 조합으로 제한 정보 조회 → 메시지 전송 횟수 확인할 때 사용
    @Query("SELECT crl FROM ChatRoomLimit crl " + "WHERE crl.chatRoomMember.chatRoom.id = :chatRoomId " + "AND crl.user.id = :userId")
    Optional<ChatRoomLimit> findLimitByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}