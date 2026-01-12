package com.siso.notification.domain.model;

import com.siso.common.domain.BaseTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto Increment
    private Long id;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "sender_nickname", nullable = false, length = 50)
    private String senderNickname;

    @Column(nullable = false, length = 50)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;   // Enum 타입 따로 정의 필요

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Builder.Default
    @Column(name = "is_read", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isRead = false;

    @Column(nullable = false, length = 255)
    private String url;
}