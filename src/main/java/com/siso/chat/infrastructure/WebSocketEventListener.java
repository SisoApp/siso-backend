package com.siso.chat.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final OnlineUserRegistry registry;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();

        // 세션 속성에서 Principal 확인 (JwtChannelInterceptor에서 저장)
        if (user == null && accessor.getSessionAttributes() != null) {
            user = (Principal) accessor.getSessionAttributes().get("user");
        }

        if (user != null) {
            log.info("[WS CONNECT] user.getName() = {}", user.getName());
            registry.addOnlineUser(user.getName());
        }
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();

        if (user == null && accessor.getSessionAttributes() != null) {
            user = (Principal) accessor.getSessionAttributes().get("user");
        }

        if (user != null) {
            log.info("[WS DISCONNECT] user.getName() = {}", user.getName());
            registry.removeOnlineUser(user.getName());
        }
    }
}
