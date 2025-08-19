package com.siso.user.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);

        if (authException instanceof RefreshTokenExpiredException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            body.put("code", "refresh_token_expired");
            body.put("message", "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
        } else if (authException instanceof BadCredentialsException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            body.put("code", "invalid_token");
            body.put("message", "유효하지 않은 토큰입니다.");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            body.put("code", "unauthorized");
            body.put("message", "인증에 실패했습니다.");
        }

        new ObjectMapper().writeValue(response.getWriter(), body);
    }
}