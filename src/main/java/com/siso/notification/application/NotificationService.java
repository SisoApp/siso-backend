package com.siso.notification.application;

import com.siso.common.firebase.application.FirebaseService;
import com.siso.common.firebase.application.FcmTokenService;
import com.siso.notification.domain.model.Notification;
import com.siso.notification.domain.model.NotificationType;
import com.siso.notification.domain.repository.NotificationRepository;
import com.siso.notification.dto.response.NotificationResponseDto;
import com.siso.notification.dto.response.UnreadCountResponseDto;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FirebaseService firebaseService;
    private final FcmTokenService fcmTokenService;
    private final UserRepository userRepository;

    /**
     * 알림을 생성하고 FCM을 통해 푸시 알림을 전송합니다.
     */
    @Transactional
    public Notification createAndSendNotification(Long receiverId,
                                                  Long senderId,
                                                  String senderNickname,
                                                  String title,
                                                  String message,
                                                  String url,
                                                  NotificationType type,
                                                  Map<String, String> extraData) {
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

            // 2. 사용자의 알림 구독 상태 확인
            User receiver = userRepository.findById(receiverId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + receiverId));

            if (!receiver.isNotificationSubscribed()) {
                log.info("User {} has disabled notifications, skipping FCM send", receiverId);
                return notification;
            }

            // 3. FCM 토큰 조회
            List<String> tokens = fcmTokenService.getActiveTokensByUserId(receiverId);
            if (tokens.isEmpty()) {
                log.warn("No active FCM tokens found for user: {}", receiverId);
                return notification;
            }

            // 4. FCM 푸시 알림 전송 (통화/메시지/매칭 모두 동일 메소드 사용)
            firebaseService.sendNotification(
                    tokens,
                    title,
                    message,
                    type.name(),
                    String.valueOf(notification.getId()),
                    url,
                    extraData
            );

            log.info("Notification sent successfully to user: {} with {} tokens", receiverId, tokens.size());
            return notification;

        } catch (Exception e) {
            log.error("Failed to send notification to user: {}", receiverId, e);
            throw e;
        }
    }

    // ========================= 조회/읽음 처리 =========================

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnreadNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UnreadCountResponseDto getUnreadCount(Long userId) {
        long count = notificationRepository.countUnreadByReceiverId(userId);
        return UnreadCountResponseDto.builder()
                .unreadCount(count)
                .build();
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Notification marked as read: {}", notificationId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByReceiverId(userId);
        log.info("All notifications marked as read for user: {}", userId);
    }

    // ========================= 알림 전송 편의 메소드 =========================

    public Notification sendMatchingNotification(Long receiverId, Long senderId, String senderNickname) {
        String title = "새로운 매칭!";
        String message = senderNickname + "님과 매칭되었습니다.";
        String url = "/matching/" + senderId;

        return createAndSendNotification(receiverId, senderId, senderNickname, title, message, url,
                NotificationType.MATCHING, null);
    }

    public Notification sendMessageNotification(Long receiverId, Long senderId, String senderNickname, String messageContent) {
        String title = senderNickname + "님의 메시지";
        String message = messageContent.length() > 50 ? messageContent.substring(0, 50) + "..." : messageContent;
        String url = "/chat/" + senderId;

        Map<String, String> extraData = Map.of(
                "senderId", String.valueOf(senderId),
                "chatRoomId", String.valueOf(senderId) // 1:1 채팅에서는 senderId를 채팅방 ID로 사용
        );

        return createAndSendNotification(receiverId, senderId, senderNickname, title, message, url,
                NotificationType.MESSAGE, extraData);
    }

    public Notification sendCallNotification(Long receiverId, Long senderId, String senderNickname,
                                             Long callId, String channelName, String agoraToken, String callerImage) {
        String title = "통화 요청";
        String message = senderNickname + "님이 통화를 요청했습니다.";
        String url = "/call/" + callId;

        Map<String, String> extraData = Map.of(
                "callId", String.valueOf(callId),
                "agoraChannel", channelName,
                "agoraToken", agoraToken,
                "callerId", String.valueOf(senderId),
                "callerName", senderNickname,
                "callerImage", callerImage,
                "timestamp", String.valueOf(System.currentTimeMillis())
        );

        return createAndSendNotification(receiverId, senderId, senderNickname, title, message, url,
                NotificationType.CALL, extraData);
    }

    // ========================= 취소/수락/거절 알림 =========================

    /**
     * 수신자에게 발신자가 통화를 취소했음을 알림
     */
    public Notification sendCallCanceledNotification(Long receiverId, Long callerId, Long callId) {
        String title = "통화 취소";
        String message = "상대방이 통화를 취소했습니다.";
        String url = "/call/" + callId;

        Map<String, String> extraData = Map.of(
                "callId", String.valueOf(callId),
                "callerId", String.valueOf(callerId),
                "status", "CANCELED",
                "timestamp", String.valueOf(System.currentTimeMillis())
        );

        return createAndSendNotification(
                receiverId,
                callerId,
                "시스템",
                title,
                message,
                url,
                NotificationType.CALL,
                extraData
        );
    }

    /**
     * 발신자에게 수신자가 통화를 수락했음을 알림
     */
    public Notification sendCallAcceptedNotification(Long receiverId, Long callId,
                                                     String channelName, String agoraToken) {
        String title = "통화 수락";
        String message = "상대방이 통화를 수락했습니다.";
        String url = "/call/" + callId;

        Map<String, String> extraData = Map.of(
                "callId", String.valueOf(callId),
                "agoraChannel", channelName,
                "agoraToken", agoraToken,
                "status", "ACCEPT",
                "timestamp", String.valueOf(System.currentTimeMillis())
        );

        // senderNickname은 null 또는 "시스템" 등으로 처리
        return createAndSendNotification(receiverId, null, "시스템", title, message, url,
                NotificationType.CALL, extraData);
    }

    /**
     * 발신자에게 수신자가 통화를 거절했음을 알림
     */
    public Notification sendCallDeniedNotification(Long receiverId, Long callId) {
        String title = "통화 거절";
        String message = "상대방이 통화를 거절했습니다.";
        String url = "/call/" + callId;

        Map<String, String> extraData = Map.of(
                "callId", String.valueOf(callId),
                "status", "DENY",
                "timestamp", String.valueOf(System.currentTimeMillis())
        );

        return createAndSendNotification(receiverId, null, "시스템", title, message, url,
                NotificationType.CALL, extraData);
    }
}
