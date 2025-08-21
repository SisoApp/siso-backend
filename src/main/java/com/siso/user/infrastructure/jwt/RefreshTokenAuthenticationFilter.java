package com.siso.user.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

public class RefreshTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper;
    private final TokenService tokenService; // 새 토큰 발급

    public RefreshTokenAuthenticationFilter(AuthenticationManager authenticationManager,
                                            JwtTokenUtil jwtTokenUtil,
                                            ObjectMapper objectMapper, TokenService tokenService) {
        super(request -> "POST".equals(request.getMethod()) && "/api/auth/refresh".equals(request.getRequestURI()));
        this.tokenService = tokenService;
        this.setAuthenticationManager(authenticationManager);
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException, IOException {
        final String refreshTokenHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(refreshTokenHeader) || !refreshTokenHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("리프레시 토큰이 제공되지 않았습니다.");
        }

        String refreshToken = refreshTokenHeader.substring(7);
        // 단순히 TokenAuthentication 객체 생성만, 검증은 Provider에서 처리
        return new TokenAuthentication(null, refreshToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {
        TokenAuthentication tokenAuth = (TokenAuthentication) authResult;

        Map<String, Object> responseBody = tokenService.refreshAccessToken((String) tokenAuth.getCredentials());

        response.setHeader("Authorization", "Bearer " + responseBody.get("accessToken"));
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), responseBody);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of("error", failed.getMessage()));
    }
}
