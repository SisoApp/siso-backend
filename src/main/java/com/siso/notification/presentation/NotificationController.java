package com.siso.notification.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.notification.application.NotificationService;
import com.siso.notification.dto.request.NotificationCreateRequestDto;
import com.siso.notification.dto.response.NotificationResponseDto;
import com.siso.notification.dto.response.UnreadCountResponseDto;
import com.siso.notification.domain.model.Notification;
import com.siso.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 알림 관리 REST API 컨트롤러 (Legacy API)
 * 
 * 알림 생성, 조회, 읽음 처리 등의 기능을 제공하는 레거시 API입니다.
 * 기존 클라이언트와의 호환성을 위해 유지됩니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API (Legacy)", description = "알림 관련 레거시 API")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 새로운 알림을 생성하고 전송합니다.
     * 
     * @param user 현재 로그인한 사용자 (발신자)
     * @param requestDto 알림 생성 요청 데이터
     * @return 생성된 알림 정보
     */
    @PostMapping
    @Operation(summary = "알림 생성", description = "새로운 알림을 생성하고 FCM을 통해 전송합니다.")
    public SisoResponse<NotificationResponseDto> createNotification(
            @CurrentUser User user,
            @RequestBody NotificationCreateRequestDto requestDto) {
        String senderNickname = user.getUserProfile() != null ? 
            user.getUserProfile().getNickname() : "익명";
        
        Notification notification = notificationService.createAndSendNotification(
                requestDto.getReceiverId(),
                user.getId(),
                senderNickname,
                requestDto.getTitle(),
                requestDto.getMessage(),
                requestDto.getUrl(),
                requestDto.getType()
        );
        return SisoResponse.success(NotificationResponseDto.fromEntity(notification));
    }

    /**
     * 현재 사용자의 모든 알림을 조회합니다.
     * 
     * @param user 현재 로그인한 사용자
     * @return 알림 목록 (최신순)
     */
    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "현재 사용자의 모든 알림을 최신순으로 조회합니다.")
    public SisoResponse<List<NotificationResponseDto>> getNotifications(@CurrentUser User user) {
        List<NotificationResponseDto> notifications = notificationService.getNotificationsByUserId(user.getId());
        return SisoResponse.success(notifications);
    }

    /**
     * 현재 사용자의 읽지 않은 알림을 조회합니다.
     * 
     * @param user 현재 로그인한 사용자
     * @return 읽지 않은 알림 목록 (최신순)
     */
    @GetMapping("/unread")
    @Operation(summary = "읽지 않은 알림 조회", description = "현재 사용자의 읽지 않은 알림을 최신순으로 조회합니다.")
    public SisoResponse<List<NotificationResponseDto>> getUnreadNotifications(@CurrentUser User user) {
        List<NotificationResponseDto> notifications = notificationService.getUnreadNotificationsByUserId(user.getId());
        return SisoResponse.success(notifications);
    }

    /**
     * 현재 사용자의 읽지 않은 알림 개수를 조회합니다.
     * 
     * @param user 현재 로그인한 사용자
     * @return 읽지 않은 알림 개수
     */
    @GetMapping("/unread/count")
    @Operation(summary = "읽지 않은 알림 개수 조회", description = "현재 사용자의 읽지 않은 알림 개수를 조회합니다.")
    public SisoResponse<UnreadCountResponseDto> getUnreadCount(@CurrentUser User user) {
        UnreadCountResponseDto unreadCount = notificationService.getUnreadCount(user.getId());
        return SisoResponse.success(unreadCount);
    }

    /**
     * 특정 알림을 읽음 처리합니다.
     * 
     * @param notificationId 읽음 처리할 알림 ID
     * @return 성공 메시지
     */
    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    public SisoResponse<Void> markAsRead(
            @Parameter(description = "알림 ID", example = "1")
            @PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return SisoResponse.success(null);
    }

    /**
     * 현재 사용자의 모든 알림을 읽음 처리합니다.
     * 
     * @param user 현재 로그인한 사용자
     * @return 성공 메시지
     */
    @PatchMapping("/read-all")
    @Operation(summary = "모든 알림 읽음 처리", description = "현재 사용자의 모든 알림을 읽음 상태로 변경합니다.")
    public SisoResponse<Void> markAllAsRead(@CurrentUser User user) {
        notificationService.markAllAsRead(user.getId());
        return SisoResponse.success(null);
    }

    /**
     * 매칭 알림을 전송합니다.
     * 
     * @param user 현재 로그인한 사용자 (발신자)
     * @param receiverId 수신자 ID
     * @return 생성된 알림 정보
     */
    @PostMapping("/matching")
    @Operation(summary = "매칭 알림 전송", description = "새로운 매칭 알림을 생성하고 전송합니다.")
    public SisoResponse<NotificationResponseDto> sendMatchingNotification(
            @CurrentUser User user,
            @Parameter(description = "수신자 ID", example = "1")
            @RequestParam Long receiverId) {
        String senderNickname = user.getUserProfile() != null ? 
            user.getUserProfile().getNickname() : "익명";
        
        Notification notification = notificationService.sendMatchingNotification(receiverId, user.getId(), senderNickname);
        return SisoResponse.success(NotificationResponseDto.fromEntity(notification));
    }

    /**
     * 통화 알림을 전송합니다.
     *
     * @param user 현재 로그인한 사용자 (발신자)
     * @param receiverId 수신자 ID
     * @return 생성된 알림 정보
     */
    @PostMapping("/call")
    @Operation(summary = "통화 알림 전송", description = "새로운 통화 알림을 생성하고 전송합니다.")
    public SisoResponse<NotificationResponseDto> sendCallNotification(
            @CurrentUser User user,
            @Parameter(description = "수신자 ID", example = "1")
            @RequestParam Long receiverId) {
        String senderNickname = user.getUserProfile() != null ? 
            user.getUserProfile().getNickname() : "익명";
        
        Notification notification = notificationService.sendCallNotification(receiverId, user.getId(), senderNickname);
        return SisoResponse.success(NotificationResponseDto.fromEntity(notification));
    }
}
