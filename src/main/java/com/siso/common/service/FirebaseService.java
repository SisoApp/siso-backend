package com.siso.common.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {
    public void sendMulticast(Collection<String> tokens,
                              String title,
                              String body,
                              String url,
                              String type,
                              String notificationId) {
        if (tokens == null || tokens.isEmpty()) return;

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("url", url)                // 앱 딥링크/딥링크 라우팅용
                .putData("type", type)              // MATCHING/MESSAGE
                .putData("notificationId", notificationId)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("FCM sent: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());
        } catch (Exception e) {
            log.error("FCM send failure", e);
        }
    }
}