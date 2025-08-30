package com.siso.notification.application;

import com.siso.common.firebase.application.FirebaseService;
import com.siso.common.firebase.application.FcmTokenService;
import com.siso.notification.domain.model.Notification;
import com.siso.notification.domain.model.NotificationType;
import com.siso.notification.domain.repository.NotificationRepository;
import com.siso.notification.dto.response.NotificationResponseDto;
import com.siso.notification.dto.response.UnreadCountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final FirebaseService firebaseService;
    private final FcmTokenService fcmTokenService;
    
    /**
     * 알림을 생성하고 FCM을 통해 푸시 알림을 전송합니다.
     */
    @Transactional
    public Notification createAndSendNotification(Long receiverId, Long senderId, String senderNickname,
                                                String title, String message, String url, NotificationType type) {
        try {
            // 1. 데이터베이스에 알림 저장
            Notification notification = Notification.builder()
                    .receiverId(receiverId)
                    .senderId(senderId)
                    .senderNickname(senderNickname)
                    .title(title)
                    .message(message)
                    .url(url)
                    .type(type)
                    .isRead(false)
                    .build();
            
            notification = notificationRepository.save(notification);
            log.info("Notification saved to database with ID: {}", notification.getId());
            
            // 2. FCM 토큰 조회
            List<String> tokens = fcmTokenService.getActiveTokensByUserId(receiverId);
            
            if (tokens.isEmpty()) {
                log.warn("No active FCM tokens found for user: {}", receiverId);
                return notification;
            }
            
            // 3. FCM 푸시 알림 전송
            firebaseService.sendMulticast(
                tokens,
                title,
                message,
                url,
                type.name(),
                String.valueOf(notification.getId())
            );
            
            log.info("Notification sent successfully to user: {} with {} tokens", receiverId, tokens.size());
            return notification;
            
        } catch (Exception e) {
            log.error("Failed to send notification to user: {}", receiverId, e);
            throw e;
        }
    }
    
    /**
     * 특정 사용자의 모든 알림을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 사용자의 읽지 않은 알림을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnreadNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 사용자의 읽지 않은 알림 개수를 조회합니다.
     */
    @Transactional(readOnly = true)
    public UnreadCountResponseDto getUnreadCount(Long userId) {
        long count = notificationRepository.countUnreadByReceiverId(userId);
        return UnreadCountResponseDto.builder()
                .unreadCount(count)
                .build();
    }
    
    /**
     * 특정 알림을 읽음 처리합니다.
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Notification marked as read: {}", notificationId);
    }
    
    /**
     * 특정 사용자의 모든 알림을 읽음 처리합니다.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByReceiverId(userId);
        log.info("All notifications marked as read for user: {}", userId);
    }
    
    /**
     * 매칭 알림을 전송합니다.
     */
    public Notification sendMatchingNotification(Long receiverId, Long senderId, String senderNickname) {
        String title = "새로운 매칭!";
        String message = senderNickname + "님과 매칭되었습니다.";
        String url = "/matching/" + senderId; // 매칭 상세 페이지 URL
        
        return createAndSendNotification(receiverId, senderId, senderNickname, title, message, url, NotificationType.MATCHING);
    }
    
        /**
     * 메시지 알림을 전송합니다.
     */
    public Notification sendMessageNotification(Long receiverId, Long senderId, String senderNickname, String messageContent) {
        String title = senderNickname + "님의 메시지";
        String message = messageContent.length() > 50 ? messageContent.substring(0, 50) + "..." : messageContent;
        String url = "/chat/" + senderId; // 채팅 페이지 URL

        return createAndSendNotification(receiverId, senderId, senderNickname, title, message, url, NotificationType.MESSAGE);
    }

    /**
     * 통화 알림을 전송합니다.
     * 클라이언트가 바로 통화에 참여할 수 있도록 필요한 모든 정보를 포함합니다.
     */
    @Transactional
    public Notification sendCallNotification(Long receiverId, Long senderId, String senderNickname,
                                             Long callId, String channelName, String agoraToken, String callerImage) {
        try {
            // 1. 데이터베이스에 알림 저장
            String title = "통화 요청";
            String message = senderNickname + "님이 통화를 요청했습니다.";
            String url = "/call/" + callId;
            
            Notification notification = Notification.builder()
                    .receiverId(receiverId)
                    .senderId(senderId)
                    .senderNickname(senderNickname)
                    .title(title)
                    .message(message)
                    .url(url)
                    .type(NotificationType.CALL)
                    .isRead(false)
                    .build();
            
            notification = notificationRepository.save(notification);
            log.info("Call notification saved to database with ID: {}", notification.getId());
            
            // 2. FCM 토큰 조회
            List<String> tokens = fcmTokenService.getActiveTokensByUserId(receiverId);
            
            if (tokens.isEmpty()) {
                log.warn("No active FCM tokens found for user: {}", receiverId);
                return notification;
            }
            
            // 3. 통화 정보를 포함한 FCM 푸시 알림 전송
            firebaseService.sendCallNotificationWithDetails(
                tokens,
                title,
                message,
                String.valueOf(callId),
                channelName,
                agoraToken,
                String.valueOf(senderId),
                senderNickname,
                callerImage
            );
            
            log.info("Call notification with details sent successfully to user: {} with {} tokens", receiverId, tokens.size());
            return notification;
            
        } catch (Exception e) {
            log.error("Failed to send call notification with details to user: {}", receiverId, e);
            throw e;
        }
    }
}