package com.siso.chat.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final OnlineUserRegistry registry;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        Principal user = StompHeaderAccessor.wrap(event.getMessage()).getUser();
        if (user != null)
            registry.addOnlineUser(user.getName());
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        Principal user = StompHeaderAccessor.wrap(event.getMessage()).getUser();
        if (user != null)
            registry.removeOnlineUser(user.getName());
    }
}
