package com.siso.chat.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Bean
    public TaskScheduler wsHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("http://13.124.11.3:8080")
                .withSockJS(); // 필요하면 Fallback
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setSendTimeLimit(15_000)               // 메시지 전송 최대 시간 (15초)
                .setSendBufferSizeLimit(512 * 1024)     // 버퍼 크기 (512KB)
                .addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                        log.error("[WebSocketTransportError] sessionId={}, cause={}", session.getId(), exception.getMessage(), exception);
                        super.handleTransportError(session, exception);
                    }
                });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue") // 구독 prefix
                .setHeartbeatValue(new long[]{20000, 20000})             // 활성화 + 20초마다 heartbeat
                .setTaskScheduler(wsHeartbeatScheduler());         // TaskScheduler
        registry.setApplicationDestinationPrefixes("/app"); // 클라 -> 서버 보낼 때 prefix
        registry.setUserDestinationPrefix("/user"); // 개인 메시지(1:1 메시지용) prefix
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}