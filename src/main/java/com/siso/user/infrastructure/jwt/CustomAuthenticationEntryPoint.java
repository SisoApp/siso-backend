package com.siso.user.infrastructure.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        System.out.println("CustomAuthenticationEntryPoint 호출");

        if (authException instanceof RefreshTokenExpiredException) {
            // 리프레시 토큰 만료 예외가 발생하면 로그인 페이지로 리다이렉트
            System.out.println("리프레시 토큰 만료 -> 로그인 페이지로 이동");
            response.sendRedirect("/view/users/login");
        } else {
            // 그 외 모든 인증 실패 시 (예: 토큰이 아예 없거나, 잘못된 토큰)
            // 401 Unauthorized 에러 응답을 반환
            System.out.println("인증 실패 -> 401 에러 반환");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증에 실패했습니다.");
        }
    }
}