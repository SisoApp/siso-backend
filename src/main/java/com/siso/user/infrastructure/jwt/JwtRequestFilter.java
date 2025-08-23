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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    private static final PathMatcher MATCHER = new AntPathMatcher();
    // 인증 예외(화이트리스트): 보안체인까지 제외(WebSecurityCustomizer)했더라도
    // 필터 단에서 한 번 더 안전하게 제외
    private static final String[] WHITELIST = {
            "/",
            "/error",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/auth/**",       // 로그인/회원가입/토큰 갱신 등
            "/oauth2/**",
            "/login/oauth2/**"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 1) CORS preflight는 필터 스킵
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // 2) 화이트리스트는 필터 스킵
        String uri = request.getRequestURI();
        for (String pattern : WHITELIST) {
            if (MATCHER.match(pattern, uri)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Authorization 없거나 Bearer 아님 → 인증 시도 없이 다음 필터
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String jwt = header.substring(7);
        try {
            // 1) 서명/만료 등 1차 검증 (구현에 따라 true/false만 주는 메소드라면 바로 사용)
            if (!jwtTokenUtil.validateToken(jwt)) {
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            // 2) 토큰 타입 확인: access 만 허용
            String tokenType = jwtTokenUtil.extractClaim(jwt, claims -> claims.get("type", String.class));
            if (!"access".equalsIgnoreCase(tokenType)) {
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            // 3) 이메일 추출 후 인증 세팅
            String email = jwtTokenUtil.extractEmail(jwt);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (ExpiredJwtException ex) {
            // 만료: 인증 세팅 없이 통과 → 보호 리소스면 EntryPoint가 401 처리
            SecurityContextHolder.clearContext();
        } catch (Exception ex) {
            // 파싱/검증 실패 등: 조용히 패스
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
