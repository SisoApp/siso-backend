package com.siso.chat.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final OnlineUserRegistry registry;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) Objects.requireNonNull(accessor.getSessionAttributes()).get("userId");
        String sessionId = accessor.getSessionId();

        if (userId != null) {
            log.info("[WS CONNECT] userId={}", userId);
            registry.addOnlineUser(userId, sessionId);
        } else {
            log.warn("[WS CONNECT] userId 찾을 수 없음, 온라인 등록 실패.");
        }
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) Objects.requireNonNull(accessor.getSessionAttributes()).get("userId");

        if (userId != null) {
            log.info("[WS DISCONNECT] userId={}", userId);
            registry.removeOnlineUser(userId);
        } else {
            log.warn("[WS DISCONNECT] userId 찾을 수 없음, 온라인 제거 실패.");
        }
    }
}
