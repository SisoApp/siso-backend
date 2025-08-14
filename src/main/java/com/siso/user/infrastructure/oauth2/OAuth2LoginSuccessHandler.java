package com.siso.user.infrastructure.oauth2;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        // phoneNumber가 사용자 식별자이므로, oAuth2User의 attributes에서 추출
        String phoneNumber = (String) oAuth2User.getAttributes().get("phoneNumber"); // 파싱 로직에 맞춰 키 변경

        String accessToken = jwtTokenUtil.generateAccessToken(phoneNumber);
        String refreshToken = jwtTokenUtil.generateRefreshToken(phoneNumber);

        // DB에 리프레시 토큰 저장
        jwtTokenUtil.storeRefreshToken(phoneNumber, refreshToken);

        // 쿠키에 토큰 담아 클라이언트로 전달
        response.addCookie(jwtTokenUtil.createCookie("accessToken", accessToken, false));
        response.addCookie(jwtTokenUtil.createCookie("refreshToken", refreshToken, true));

        // 클라이언트로 리다이렉트
        response.sendRedirect("http://localhost:3000/oauth-callback");
    }
}
