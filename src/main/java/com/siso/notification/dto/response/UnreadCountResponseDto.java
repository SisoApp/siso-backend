package com.siso.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 읽지 않은 알림 개수 응답 DTO
 * 
 * 사용자의 읽지 않은 알림 개수를 응답할 때 사용됩니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponseDto {
    
    @Schema(description = "읽지 않은 알림 개수", example = "5")
    private long unreadCount;
}
