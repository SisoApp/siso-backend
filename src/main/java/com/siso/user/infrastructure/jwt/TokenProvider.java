package com.siso.user.infrastructure.jwt;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class TokenProvider implements AuthenticationProvider {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        System.out.println("TokenProvider.authenticate");

        String phoneNumber = (String) authentication.getPrincipal();
        String refreshToken = (String) authentication.getCredentials();

        // DB에서 사용자 정보와 리프레시 토큰 조회
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        String foundRefreshToken = user.getRefreshToken();

        if (foundRefreshToken == null || foundRefreshToken.isBlank()) {
            throw new ExpectedException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        try {
            jwtTokenUtil.isTokenExpired(foundRefreshToken);
        } catch (ExpiredJwtException ex) {
            throw new ExpectedException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        if (!foundRefreshToken.equals(refreshToken)) {
            throw new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 최종 인증 객체 반환
        return new TokenAuthentication(
                user.getPhoneNumber(),
                user.getRefreshToken(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // 임의 권한 부여
        );
    }

    @Override
    public boolean supports(Class<?> aclass) {
        return TokenAuthentication.class.isAssignableFrom(aclass);
    }
}