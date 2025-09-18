package com.siso.chat.infrastructure;

import com.siso.user.domain.model.User;
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

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token != null && jwtTokenUtil.validateToken(token)) {
                User user = jwtTokenUtil.validateAndGetUserId(token); // 토큰 검증 및 User 조회
                log.info("[JwtChannelInterceptor] Authenticated user: {} (id={})", user.getEmail(), user.getId());

                AccountAdapter account = new AccountAdapter(user);

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

