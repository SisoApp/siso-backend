package com.siso.chat.domain.model;

import com.siso.call.domain.model.Call;
import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_rooms")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    private User receiverId;
//
//    private User senderId;
//
//    private Call callId;
//
//    private ChatMessage lastMessageId;

}
