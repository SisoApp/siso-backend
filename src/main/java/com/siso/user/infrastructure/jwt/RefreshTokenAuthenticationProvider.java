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
        String refreshToken = (String) authentication.getCredentials();

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 토큰 만료 및 타입 체크
        if (jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new ExpectedException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        if (!jwtTokenUtil.isRefreshToken(refreshToken)) {
            throw new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return new TokenAuthentication(new AccountAdapter(user), refreshToken,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TokenAuthentication.class.isAssignableFrom(authentication);
    }
}