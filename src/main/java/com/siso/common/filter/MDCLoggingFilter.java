package com.siso.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * MDC (Mapped Diagnostic Context) Logging Filter
 * - 각 HTTP 요청마다 고유한 Request ID 생성
 * - 로그 추적을 위한 컨텍스트 정보 저장 (userId, userEmail 등)
 * - 분산 환경에서 요청 추적 용이
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCLoggingFilter implements Filter {

    private static final String REQUEST_ID = "requestId";
    private static final String USER_ID = "userId";
    private static final String USER_EMAIL = "userEmail";
    private static final String REQUEST_URI = "requestUri";
    private static final String REQUEST_METHOD = "requestMethod";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // Request ID 생성 (헤더에 있으면 사용, 없으면 생성)
            String requestId = httpRequest.getHeader("X-Request-ID");
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }

            // MDC에 컨텍스트 정보 저장
            MDC.put(REQUEST_ID, requestId);
            MDC.put(REQUEST_URI, httpRequest.getRequestURI());
            MDC.put(REQUEST_METHOD, httpRequest.getMethod());

            // 요청 시작 로그
            log.info("Request started: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

            chain.doFilter(request, response);

            // 요청 완료 로그
            log.info("Request completed: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

        } finally {
            // MDC 클리어 (메모리 누수 방지)
            MDC.clear();
        }
    }

    /**
     * 사용자 정보를 MDC에 추가
     * - 인증 후 컨트롤러나 서비스에서 호출
     */
    public static void setUser(Long userId, String userEmail) {
        if (userId != null) {
            MDC.put(USER_ID, userId.toString());
        }
        if (userEmail != null) {
            MDC.put(USER_EMAIL, userEmail);
        }
    }

    /**
     * 현재 요청의 Request ID 조회
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID);
    }
}
