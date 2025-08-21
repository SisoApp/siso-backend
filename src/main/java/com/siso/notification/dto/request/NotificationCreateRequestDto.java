package com.siso.notification.dto.request;

import com.siso.notification.domain.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 생성 요청 DTO
 * 
 * 새로운 알림을 생성할 때 사용되는 요청 데이터를 담습니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequestDto {
    
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
}
