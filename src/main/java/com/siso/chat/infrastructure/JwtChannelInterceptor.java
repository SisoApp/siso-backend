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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor == null) {
                // heartbeat 등은 accessor가 null일 수 있음 — 안전히 통과
                return message;
            }

            StompCommand command = accessor.getCommand();
            if (StompCommand.CONNECT.equals(command)) {
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

                    // 세션 속성이 null일 수 있으므로 안전하게 초기화
                    Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
                    if (sessionAttrs == null) {
                        sessionAttrs = new ConcurrentHashMap<>();
                        accessor.setSessionAttributes(sessionAttrs);
                    }
                    sessionAttrs.put("user", auth);
                    sessionAttrs.put("userId", user.getId().toString());
                } else {
                    log.warn("[JwtChannelInterceptor] Missing or invalid token: {}", token);
                    // 토큰이 유효하지 않다면 CONNECT를 거부하려면 null 반환 (클라이언트는 연결 실패)
                    return null;
                }
            }
        } catch (Exception ex) {
            // 절대 예외를 던지지 말 것 — transport error/1002 원인
            log.error("[JwtChannelInterceptor] 예외 발생(메시지 차단 대신 통과): ", ex);
            // 토큰 검증 과정에서 보안상 치명적 문제
            return null;
        }
        return message;
    }
}


