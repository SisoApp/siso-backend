package com.siso.notification.dto.response;

import com.siso.notification.domain.model.Notification;
import com.siso.notification.domain.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 * 
 * 알림 조회 시 클라이언트에게 전달되는 응답 데이터를 담습니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    
    @Schema(description = "알림 ID", example = "1")
    private Long id;
    
    @Schema(description = "수신자 ID", example = "1")
    private Long receiverId;
    
    @Schema(description = "발신자 ID", example = "2")
    private Long senderId;
    
    @Schema(description = "발신자 닉네임", example = "김철수")
    private String senderNickname;
    
    @Schema(description = "알림 제목", example = "새로운 매칭!")
    private String title;
    
    @Schema(description = "알림 내용", example = "김철수님과 매칭되었습니다.")
    private String message;
    
    @Schema(description = "이동할 URL", example = "/matching/2")
    private String url;
    
    @Schema(description = "알림 타입", example = "MATCHING")
    private NotificationType type;
    
    @Schema(description = "읽음 여부", example = "false")
    private boolean isRead;
    
    @Schema(description = "생성 시간", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    /**
     * Notification 엔티티를 DTO로 변환합니다.
     * 
     * @param notification 변환할 알림 엔티티
     * @return 변환된 응답 DTO
     */
    public static NotificationResponseDto fromEntity(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .receiverId(notification.getReceiverId())
                .senderId(notification.getSenderId())
                .senderNickname(notification.getSenderNickname())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .url(notification.getUrl())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
