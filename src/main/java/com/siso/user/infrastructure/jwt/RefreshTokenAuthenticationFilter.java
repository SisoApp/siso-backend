package com.siso.user.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siso.user.dto.response.TokenResponseDto;
import com.siso.user.infrastructure.authentication.AccountAdapter;
import io.jsonwebtoken.ExpiredJwtException;
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

public class RefreshTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService; // 새 토큰 발급

    public RefreshTokenAuthenticationFilter(AuthenticationManager authenticationManager,
                                            JwtTokenUtil jwtTokenUtil,
                                            ObjectMapper objectMapper, TokenService tokenService) {
        super(request -> "POST".equals(request.getMethod()) && "/api/auth/refresh".equals(request.getRequestURI()));
        this.tokenService = tokenService;
        this.setAuthenticationManager(authenticationManager);
        this.jwtTokenUtil = jwtTokenUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {

        final String refreshTokenHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(refreshTokenHeader) || !refreshTokenHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("리프레시 토큰이 제공되지 않았습니다.");
        }

        String refreshToken = refreshTokenHeader.substring(7);

        String email;
        try {
            email = jwtTokenUtil.extractEmail(refreshToken); // 이메일 기준
        } catch (ExpiredJwtException e) {
            throw new RefreshTokenExpiredException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
        } catch (Exception e) {
            throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
        }

        TokenAuthentication authRequest = new TokenAuthentication(email, refreshToken);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {
        TokenAuthentication tokenAuth = (TokenAuthentication) authResult;

        // TokenService에서 새 AccessToken + RefreshToken 발급
        TokenResponseDto tokenResponse = tokenService.refreshAccessToken((String) tokenAuth.getCredentials());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), tokenResponse);
    }
}
