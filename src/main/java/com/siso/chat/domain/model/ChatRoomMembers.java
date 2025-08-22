package com.siso.chat.domain.model;

import com.siso.user.domain.model.User;
import jakarta.persistence.*;

public class ChatRoomMembers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private ChatRoom chatRoom;
}
