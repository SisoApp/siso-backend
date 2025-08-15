package com.siso.user.infrastructure.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

// 로그인할 때 제외하고 security context에 사용자 정보 넣는 로직
// 없으면 null, 있으면 정보 넣기
// * 액세스 토큰 만료 시 익명 사용자로 설정
//  -> AuthenticationEntryPoint에서 (Get /api/auth/refresh) 리다이렉트 구현
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String phoneNumber = null;
        String jwt = null;

        // 1. Authorization 헤더에서 JWT 추출
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                // 2. JWT에서 사용자 식별자(전화번호) 추출
                phoneNumber = jwtTokenUtil.extractPhoneNumber(jwt);
            } catch (ExpiredJwtException e) {
                // 토큰 만료 시, 인증 정보 없이 다음 필터로 진행.
                // CustomAuthenticationEntryPoint가 401 Unauthorized를 처리할 것입니다.
                chain.doFilter(request, response);
                return;
            } catch (Exception e) {
                // 토큰 파싱 실패 등 다른 예외 발생 시, 401 에러를 발생시켜야 함
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("잘못된 JWT 토큰.");
                return;
            }
        }

        // 3. 사용자 식별자가 있고, SecurityContext에 인증 정보가 없는 경우
        if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // UserDetails 로드
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(phoneNumber);

            // 토큰 유효성 검사 (만료 여부는 이미 위에서 처리)
            if (jwtTokenUtil.validateToken(jwt)) {
                // UsernamePasswordAuthenticationToken 생성 및 SecurityContext에 저장
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 다음 필터로 진행
        chain.doFilter(request, response);
    }
}