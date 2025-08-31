package com.siso.chat.domain.repository;

import com.siso.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // call.id 로 ChatRoom 찾기
    Optional<ChatRoom> findByCallId(Long callId);
    // 사용자가 속한 채팅방 조회
    @Query("SELECT cr FROM ChatRoom cr " + "JOIN cr.chatRoomMembers m " + "WHERE m.user.id = :userId")
    List<ChatRoom> findRoomsByUserId(@Param("userId") Long userId);
}
