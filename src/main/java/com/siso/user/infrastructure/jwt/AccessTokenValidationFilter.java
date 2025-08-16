package com.siso.user.infrastructure.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 리프레시 토큰따라 entrypoint에서 예외처리 세분화
// 액세스 토큰 유효하면 다음 필터로 넘어가기
// 액세스 토큰이 있는데 만료된 사용자 -> 갱신하도록 리다이렉트
// 보호된 리소스에 익명 사용자 -> 로그인 페이지 redirect
@Component
@RequiredArgsConstructor
public class AccessTokenValidationFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String phoneNumber = null;
        String accessToken = null;

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
            try {
                phoneNumber = jwtTokenUtil.extractPhoneNumber(accessToken);
            } catch (ExpiredJwtException e) {
                // 액세스 토큰 만료 시, 다음 필터로 진행하여 401 Unauthorized 처리
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                // 토큰 파싱 실패 등 기타 예외 발생 시 다음 필터로 진행
                throw new AccessTokenExpiredException("액세스 토큰이 만료되었습니다.");
            }
        }

        if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(phoneNumber);
            if (jwtTokenUtil.validateToken(accessToken)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}