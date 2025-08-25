package com.siso.user.infrastructure.jwt;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;
import java.util.List;

public record JwtChannelInterceptor(JwtTokenUtil jwtTokenUtil) implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && accessor.getCommand() != null) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String token = authHeaders.get(0).replace("Bearer ", "");
                if (jwtTokenUtil.validateToken(token)) {
                    String userId = String.valueOf(jwtTokenUtil.validateAndGetUserId(token));

                    // Authentication → Principal 래핑
                    Principal principal = (Principal) () -> userId;
                    accessor.setUser(principal);
                }
            }
        }
        return message;
    }
}
