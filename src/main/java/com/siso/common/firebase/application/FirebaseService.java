package com.siso.common.firebase.application;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * Firebase Cloud Messaging (FCM) 서비스 클래스
 * 
 * FCM을 통해 Android/iOS 디바이스에 푸시 알림을 전송하는 기능을 제공합니다.
 * 여러 디바이스에 동시에 알림을 전송할 수 있는 멀티캐스트 메시징을 지원합니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

    public void sendNotification(Collection<String> tokens,
                                 String title,
                                 String body,
                                 String type,
                                 String notificationId,
                                 String url,
                                 Map<String, String> extraData) {

        if (tokens == null || tokens.isEmpty()) {
            log.warn("No FCM tokens provided, skipping notification send");
            return;
        }

        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("type", type)
                .putData("notificationId", notificationId);

        if (url != null) messageBuilder.putData("url", url);

        // 추가 데이터가 있으면 모두 삽입
        if (extraData != null) {
            extraData.forEach(messageBuilder::putData);
        }

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(messageBuilder.build());
            log.info("Notification sent: type={}, success={}, failure={}", type, response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                log.warn("FCM send failed for {} tokens", response.getFailureCount());
            }
        } catch (Exception e) {
            log.error("Failed to send notification of type {}", type, e);
        }
    }

    /**
     * 통화 알림을 전송합니다 (통화 정보 포함).
     * 클라이언트가 바로 통화에 참여할 수 있도록 필요한 모든 정보를 포함합니다.
     */
    public void sendCallNotificationWithDetails(Collection<String> tokens,
                                               String title,
                                               String body,
                                               String callId,
                                               String channelName,
                                               String agoraToken,
                                               String callerId,
                                               String callerNickname,
                                               String callerImage) {
        if (tokens == null || tokens.isEmpty()) {
            log.warn("No FCM tokens provided for call notification");
            return;
        }

        log.info("Sending call notification with details: callId={}, tokenCount={}", callId, tokens.size());

        // 통화 정보를 포함한 FCM 메시지 생성
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                // 통화에 필요한 모든 정보 포함 (클라이언트 데이터 클래스에 맞춤)
                .putData("type", "CALL")
                .putData("callId", callId)
                .putData("agoraChannel", channelName)        // channelName → agoraChannel로 변경
                .putData("agoraToken", agoraToken)
                .putData("callerName", callerNickname)       // callerNickname → callerName으로 변경        // 실제 이미지 URL 전달
                .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("Call notification with details sent: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());
            
            if (response.getFailureCount() > 0) {
                log.warn("FCM send failed for {} tokens", response.getFailureCount());
            }
        } catch (Exception e) {
            log.error("Failed to send call notification with details for {} tokens", tokens.size(), e);
        }
    }
}