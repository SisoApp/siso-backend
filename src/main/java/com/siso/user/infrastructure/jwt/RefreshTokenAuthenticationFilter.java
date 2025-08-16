package com.siso.user.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siso.user.dto.response.TokenResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * RefreshTokenAuthenticationFilter는 /api/auth/refresh 요청을 가로채서
 * 리프레시 토큰을 검증하고 새로운 액세스 토큰을 발급하는 역할을 합니다.
 */
public class RefreshTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;

    /**
     * 필터가 처리할 경로와 HTTP 메서드를 지정하고, 필요한 의존성을 주입합니다.
     * PathRequestMatcher를 사용하여 최신 Spring Security의 권장 사항을 따릅니다.
     *
     * @param authenticationManager Spring Security 인증 관리자
     * @param jwtTokenUtil JWT 토큰 유틸리티 클래스
     * @param objectMapper JSON 직렬화를 위한 ObjectMapper
     */
    public RefreshTokenAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, ObjectMapper objectMapper) {
        super(new RequestMatcher() {
            @Override
            public boolean matches(HttpServletRequest request) {
                // HTTP 메서드가 POST이고 요청 경로가 "/api/auth/refresh"인지 확인
                return "POST".equals(request.getMethod()) && "/api/auth/refresh".equals(request.getRequestURI());
            }
        });
        this.setAuthenticationManager(authenticationManager);
        this.jwtTokenUtil = jwtTokenUtil;
        this.objectMapper = objectMapper;
    }

    /**
     * 인증을 시도하는 메서드. HTTP 요청에서 리프레시 토큰을 추출하고 유효성을 검증합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 인증된 Authentication 객체
     * @throws AuthenticationException 인증 실패 시 발생
     * @throws IOException 입출력 예외 발생 시
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        final String refreshTokenHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(refreshTokenHeader) && refreshTokenHeader.startsWith("Bearer ")) {
            String refreshToken = refreshTokenHeader.substring(7);

            // 리프레시 토큰에서 전화번호 추출 (validateToken 로직에 따라 예외 처리 필요)
            String phoneNumber;
            try {
                phoneNumber = jwtTokenUtil.extractPhoneNumber(refreshToken);
            } catch (ExpiredJwtException e) {
                // 만료된 토큰의 경우, 인증 실패 예외를 던져 EntryPoint에서 처리
                throw new RefreshTokenExpiredException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
            } catch (Exception e) {
                // 서명 오류 등 기타 예외 발생 시
                throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
            }

            // 올바른 principal과 credentials로 TokenAuthentication 객체 생성
            TokenAuthentication authRequest = new TokenAuthentication(phoneNumber, refreshToken);
            return this.getAuthenticationManager().authenticate(authRequest);
        }

        // 헤더에 토큰이 없는 경우
        throw new BadCredentialsException("리프레시 토큰이 제공되지 않았습니다.");
    }

//    /**
//     * 인증 성공 시 호출되는 메서드로, 새로운 액세스 토큰을 응답으로 작성합니다.
//     * 최신 버전에서는 FilterChain 파라미터가 제거되었습니다.
//     *
//     * @param request HTTP 요청
//     * @param response HTTP 응답
//     * @param authResult 인증 성공 결과 객체
//     * @throws IOException 입출력 예외 발생 시
//     */
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
//        // Authentication 객체의 Principal에 담긴 새로운 액세스 토큰을 추출
//        String newAccessToken = (String) authResult.getPrincipal();
//        response.setStatus(HttpServletResponse.SC_OK);
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//
//        // 새로운 토큰을 JSON 형태로 응답에 작성
//        objectMapper.writeValue(response.getWriter(), new TokenResponseDto(newAccessToken, null));
//    }
}
