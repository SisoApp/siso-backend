package com.siso.chat.domain.model;

import com.siso.call.domain.model.Call;
import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@EqualsAndHashCode(callSuper = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "call_id", nullable = false)
    private Call call;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_status", nullable = false)
    private ChatRoomStatus chatRoomStatus;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomMember> chatRoomMembers = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    // 양방향 연관 관계 설정
    public void linkCall(Call call) {
        this.call = call;
        call.linkChatRoom(this);
    }

    public void addChatRoomMember(ChatRoomMember chatRoomMember) {
        chatRoomMembers.add(chatRoomMember);
        chatRoomMember.linkChatRoom(this);
    }

    public void addChatMessage(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
        chatMessage.linkChatRoom(this);
    }

    @Builder
    public  ChatRoom(Call call, ChatRoomStatus chatRoomStatus) {
        this.call = call;
        this.chatRoomStatus = chatRoomStatus;
    }

    public void updateChatRoomStatus(ChatRoomStatus chatRoomStatus) {
        this.chatRoomStatus = chatRoomStatus;
    }
}
