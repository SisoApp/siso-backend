package com.siso.chat.infrastructure;

import com.siso.user.infrastructure.authentication.AccountAdapter;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(Objects.requireNonNull(accessor).getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            log.info("[JwtChannelInterceptor] CONNECT Authorization header: {}", token);

            if (token != null && jwtTokenUtil.validateToken(token)) {
                AccountAdapter account = jwtTokenUtil.getAccountFromToken(token);
                log.info("[JwtChannelInterceptor] Authenticated user: {}", account.getUsername());

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
                accessor.setUser(auth);
            } else {
                log.warn("[JwtChannelInterceptor] Missing or invalid token: {}", token);
            }
        }
        return message;
    }
}

