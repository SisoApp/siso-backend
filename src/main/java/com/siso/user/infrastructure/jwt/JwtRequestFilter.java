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

    // 허용(인증 불필요) 경로들
    private static final PathMatcher MATCHER = new AntPathMatcher();
    private static final String[] WHITELIST = {
            "/",
            "/error",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/auth/**",
            "/oauth2/**",
            "/login/oauth2/**"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : WHITELIST) {
            if (MATCHER.match(pattern, uri)) {
                return true; // 화이트리스트는 아예 필터에서 제외
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Authorization 헤더가 없거나 Bearer 가 아니면 인증 시도하지 않고 바로 다음 필터로
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String jwt = header.substring(7);
        try {
            // 이메일 추출 및 토큰 타입 확인
            String email = jwtTokenUtil.extractEmail(jwt);
            String tokenType = jwtTokenUtil.extractClaim(jwt, claims -> claims.get("type", String.class));

            // access 토큰만 허용. 아니면 인증 세팅 없이 통과(보호 API면 EntryPoint가 401 응답함)
            if (!"access".equals(tokenType)) {
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            // 아직 인증 안 되어 있으면 SecurityContext 세팅
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtTokenUtil.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (ExpiredJwtException ex) {
            // 만료/오류 토큰은 인증 세팅 없이 그냥 통과 (보호 API 접근 시 EntryPoint가 401 처리)
            SecurityContextHolder.clearContext();
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}