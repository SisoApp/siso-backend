package com.siso.chat.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final OnlineUserRegistry registry;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        String sessionId = accessor.getSessionId();

        String userId = null;
        if (sessionAttrs != null) {
            Object uid = sessionAttrs.get("userId");
            if (uid != null) userId = String.valueOf(uid);
        }
        // fallback: accessor.getUser()에서 Principal 조회
        if (userId == null && accessor.getUser() != null) {
            userId = accessor.getUser().getName();
        }

        if (userId != null) {
            log.info("[WS CONNECT] userId={} sessionId={}", userId, sessionId);
            registry.addOnlineUser(userId, sessionId);
        } else {
            log.warn("[WS CONNECT] userId 찾을 수 없음, 온라인 등록 실패. sessionId={}", sessionId);
        }
    }

    @EventListener
    public void handleSessionConnectedEvent(SessionConnectedEvent event) {
        log.info("[WS CONNECTED] sessionId={} user={}",
                event.getMessage().getHeaders().get("simpSessionId"),
                event.getUser());
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        CloseStatus status = event.getCloseStatus();
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor != null ? accessor.getSessionId() : null;

        String userId = null;
        Map<String, Object> sessionAttrs = accessor != null ? accessor.getSessionAttributes() : null;
        if (sessionAttrs != null) {
            Object uid = sessionAttrs.get("userId");
            if (uid != null) userId = String.valueOf(uid);
        }
        if (userId == null && accessor != null && accessor.getUser() != null) {
            userId = accessor.getUser().getName();
        }

        if (userId != null) {
            log.info("[WS DISCONNECT] userId={} sessionId={} CloseStatus: code={}, reason='{}', at={}", userId, sessionId, status.getCode(), status.getReason(), LocalDateTime.now());
            // sessionId 기준으로 제거 (멀티 세션 지원)
            registry.removeOnlineUser(userId, sessionId);
        } else if (sessionId != null) {
            log.warn("[WS DISCONNECT] userId 못 찾음. sessionId={} -> 강제로 제거 시도", sessionId);
            registry.removeBySessionId(sessionId);
        } else {
            log.warn("[WS DISCONNECT] sessionId/ userId 모두 없음, 제거 불가");
        }
    }
}

