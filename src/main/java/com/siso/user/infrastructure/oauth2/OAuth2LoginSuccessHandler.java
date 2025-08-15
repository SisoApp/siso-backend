package com.siso.user.infrastructure.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.response.TokenResponseDto;
import com.siso.user.infrastructure.authentication.AccountAdapter;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        AccountAdapter accountAdapter = (AccountAdapter) authentication.getPrincipal();
        User user = accountAdapter.getUser();

        // 1. JWT 액세스 토큰 및 리프레시 토큰 생성
        String accessToken = jwtTokenUtil.generateAccessToken(user.getPhoneNumber());
        String refreshToken = jwtTokenUtil.generateRefreshToken();

        // 2. 리프레시 토큰을 DB에 저장
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        // 3. 클라이언트로 토큰 전송 (예시: JSON 바디)
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getWriter(), new TokenResponseDto(accessToken, refreshToken));
    }
}