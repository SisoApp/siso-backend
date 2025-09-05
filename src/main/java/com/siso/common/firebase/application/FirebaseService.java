package com.siso.common.firebase.application;

import com.google.firebase.messaging.*;
import com.siso.common.firebase.infrastructure.MulticastResultConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
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

    private static final int DEFAULT_BATCH_SIZE = 500;   // FCM 권장 상한
    private final MulticastResultConfig multicast;       // 배치 멀티캐스트 헬퍼

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

        // 데이터 페이로드 구성
        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", type);
        data.put("notificationId", notificationId);
        if (url != null) data.put("url", url);
        if (extraData != null && !extraData.isEmpty()) data.putAll(extraData);

        // 배치 전송 + 결과 요약
        MulticastResultConfig.SendMulticastResult result = multicast.sendMulticastResult(
                tokens, title, body, data, DEFAULT_BATCH_SIZE, false
        );

        log.info("Notification sent: type={}, requested={}, success={}, failure={}, invalidTokens={}",
                type, result.getRequestedCount(), result.getSuccessCount(),
                result.getFailureCount(), result.getInvalidTokens().size());

        if (!result.getInvalidTokens().isEmpty()) {
            // TODO: 만료/무효 토큰 정리 로직 연결 (예: DB에서 삭제)
            log.warn("Invalid FCM tokens detected: {}", result.getInvalidTokens().size());
        }
    }

    /**
     * 통화 알림 전송 (통화 정보 포함)
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

        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", "CALL");
        data.put("callId", callId);
        data.put("agoraChannel", channelName);
        data.put("agoraToken", agoraToken);
        data.put("callerId", callerId);
        data.put("callerName", callerNickname);
        data.put("callerImage", callerImage);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        MulticastResultConfig.SendMulticastResult result = multicast.sendMulticastResult(
                tokens, title, body, data, DEFAULT_BATCH_SIZE, false
        );

        log.info("Call notification sent: requested={}, success={}, failure={}, invalidTokens={}",
                result.getRequestedCount(), result.getSuccessCount(),
                result.getFailureCount(), result.getInvalidTokens().size());

        if (!result.getInvalidTokens().isEmpty()) {
            // TODO: 만료/무효 토큰 정리 로직 연결
            log.warn("Invalid FCM tokens detected (CALL): {}", result.getInvalidTokens().size());
        }
    }
}