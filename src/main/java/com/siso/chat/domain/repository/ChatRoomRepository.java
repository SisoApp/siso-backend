package com.siso.chat.domain.repository;

import com.siso.chat.domain.model.ChatRoom;
import com.siso.user.domain.model.User;
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

    // 채팅방 멤버들 사이에 채팅방 존재 여부 확인
    @Query("""
           SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
           FROM ChatRoom c
           JOIN c.chatRoomMembers m1
           JOIN c.chatRoomMembers m2
           WHERE m1.user = :user1
             AND m2.user = :user2
             AND SIZE(c.chatRoomMembers) = 2
           """)
    boolean existsByMembers(@Param("user1") User user1, @Param("user2") User user2);
}