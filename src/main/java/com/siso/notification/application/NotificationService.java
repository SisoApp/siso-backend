package com.siso.notification.application;

import com.siso.common.firebase.application.FirebaseService;
import com.siso.common.firebase.application.FcmTokenService;
import com.siso.notification.domain.model.Notification;
import com.siso.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final FirebaseService firebaseService;
    private final FcmTokenService fcmTokenService;
    
    /**
     * 알림을 생성하고 FCM을 통해 푸시 알림을 전송합니다.
     */
    @Transactional
    public void createAndSendNotification(Long receiverId, Long senderId, String senderNickname,
                                        String title, String message, String url, NotificationType type) {
        try {
            // 1. 데이터베이스에 알림 저장 (기존 코드가 있다면 해당 로직 사용)
            // Notification notification = saveNotification(receiverId, senderId, senderNickname, title, message, url, type);
            
            // 2. FCM 토큰 조회
            List<String> tokens = fcmTokenService.getActiveTokensByUserId(receiverId);
            
            if (tokens.isEmpty()) {
                log.warn("No active FCM tokens found for user: {}", receiverId);
                return;
            }
            
            // 3. FCM 푸시 알림 전송
            firebaseService.sendMulticast(
                tokens,
                title,
                message,
                url,
                type.name(),
                String.valueOf(System.currentTimeMillis())
            );
            
            log.info("Notification sent successfully to user: {} with {} tokens", receiverId, tokens.size());
            
        } catch (Exception e) {
            log.error("Failed to send notification to user: {}", receiverId, e);
        }
    }
    
    /**
     * 매칭 알림을 전송합니다.
     */
    public void sendMatchingNotification(Long receiverId, Long senderId, String senderNickname) {
        String title = "새로운 매칭!";
        String message = senderNickname + "님과 매칭되었습니다.";
        String url = "/matching/" + senderId; // 매칭 상세 페이지 URL
        
        createAndSendNotification(receiverId, senderId, senderNickname, title, message, url, NotificationType.MATCHING);
    }
    
    /**
     * 메시지 알림을 전송합니다.
     */
    public void sendMessageNotification(Long receiverId, Long senderId, String senderNickname, String messageContent) {
        String title = senderNickname + "님의 메시지";
        String message = messageContent.length() > 50 ? messageContent.substring(0, 50) + "..." : messageContent;
        String url = "/chat/" + senderId; // 채팅 페이지 URL
        
        createAndSendNotification(receiverId, senderId, senderNickname, title, message, url, NotificationType.MESSAGE);
    }
}
