package com.siso.user.infrastructure.jwt;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.infrastructure.authentication.AccountAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
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
        // 1. credentials: refresh token
        String refreshToken = (String) authentication.getCredentials();
        System.out.println("refreshTokenrefreshTokenrefreshTokenrefreshTokenrefreshTokenrefreshTokenrefreshToken: " + refreshToken);

        // 2. DB에서 사용자 조회
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 3. 토큰 만료 확인
        if (jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new ExpectedException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 4. 새 AccessToken/RefreshToken 생성은 TokenService에서 처리
        return new TokenAuthentication(
                new AccountAdapter(user),
                refreshToken, // credentials 그대로 전달
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TokenAuthentication.class.isAssignableFrom(authentication);
    }
}