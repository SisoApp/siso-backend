package com.siso.common.firebase.application;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

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
    
    /**
     * 여러 디바이스에 FCM 푸시 알림을 전송합니다.
     * 
     * @param tokens FCM 토큰 목록 (각 토큰은 하나의 디바이스를 나타냄)
     * @param title 알림 제목
     * @param body 알림 내용
     * @param url 딥링크 URL (앱 내 특정 페이지로 이동할 때 사용)
     * @param type 알림 타입 (MATCHING, MESSAGE 등)
     * @param notificationId 알림 고유 식별자
     */
    public void sendMulticast(Collection<String> tokens,
                              String title,
                              String body,
                              String url,
                              String type,
                              String notificationId) {
        // 토큰이 없으면 전송하지 않음
        if (tokens == null || tokens.isEmpty()) {
            log.warn("No FCM tokens provided, skipping notification send");
            return;
        }

        // FCM 멀티캐스트 메시지 생성
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)  // 대상 디바이스 토큰들 추가
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("url", url)                // 앱 딥링크/딥링크 라우팅용
                .putData("type", type)              // 알림 타입 (MATCHING/MESSAGE)
                .putData("notificationId", notificationId)  // 알림 고유 ID
                .build();

        try {
            // FCM 서비스를 통해 메시지 전송
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("FCM sent: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());
            
            // 실패한 토큰들에 대한 상세 로그 (선택사항)
            if (response.getFailureCount() > 0) {
                log.warn("FCM send failed for {} tokens", response.getFailureCount());
            }
        } catch (Exception e) {
            log.error("FCM send failure", e);
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
                .putData("callerName", callerNickname)       // callerNickname → callerName으로 변경
                .putData("callerImage", callerImage)         // 실제 이미지 URL 전달
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