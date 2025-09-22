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

import java.io.EOFException;

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
                .setAllowedOriginPatterns("http://13.124.11.3:8080");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
                .setMessageSizeLimit(128 * 1024)        // 개별 메시지 최대 128KB
                .setSendBufferSizeLimit(512 * 1024)    // 버퍼 사이즈 512KB
                .setSendTimeLimit(20_000)              // 20초까지 전송 허용
                .addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                        if (exception instanceof EOFException) {
                            log.warn("[WebSocket EOF] sessionId={}", session.getId());
                        } else {
                            log.error("[WebSocketTransportError] sessionId={}, cause={}", session.getId(), exception.getMessage(), exception);
                        }
                        super.handleTransportError(session, exception);
                    }
                });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue") // 구독 prefix
                .setHeartbeatValue(new long[]{10_000, 10_000})             // 활성화 + 10초마다 heartbeat
                .setTaskScheduler(wsHeartbeatScheduler());         // TaskScheduler
        registry.setApplicationDestinationPrefixes("/app"); // 클라 -> 서버 보낼 때 prefix
        registry.setUserDestinationPrefix("/user"); // 개인 메시지(1:1 메시지용) prefix
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration
                .taskExecutor()
                .corePoolSize(4)    // 동시에 처리할 최소 스레드 수
                .maxPoolSize(8)    // 최대 스레드 수
                .queueCapacity(500) // 대기열
                .keepAliveSeconds(60);
        registration.interceptors(jwtChannelInterceptor);
    }
}