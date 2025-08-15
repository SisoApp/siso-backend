package com.siso.user.infrastructure.jwt;

import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class RefreshTokenAuthenticationProvider implements AuthenticationProvider {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String refreshToken = (String) authentication.getPrincipal();

        // 1. JWT 자체 유효성 검사 (만료 여부, 서명)
        if (!jwtTokenUtil.validateToken(refreshToken)) {
            throw new BadCredentialsException("Refresh token is expired or invalid.");
        }

        // 2. DB에서 리프레시 토큰으로 사용자 조회
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token."));

        // 3. 새로운 액세스 토큰 생성
        String newAccessToken = jwtTokenUtil.generateAccessToken(user.getPhoneNumber());

        // 4. 새로운 Authentication 객체에 액세스 토큰과 권한을 담아 반환
        return new TokenAuthentication(newAccessToken, Collections.singletonList(new SimpleGrantedAuthority("USER")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TokenAuthentication.class.isAssignableFrom(authentication);
    }
}