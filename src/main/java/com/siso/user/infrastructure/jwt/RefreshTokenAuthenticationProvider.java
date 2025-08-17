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

        // 2. refreshToken에서 email(subject) 추출
        String email;
        try {
            email = jwtTokenUtil.getEmailFromToken(refreshToken);
            System.out.println("Refresh Token에서 추출된 email: " + email);
        } catch (Exception e) {
            throw new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN); // 토큰이 잘못됐거나 파싱 실패
        }

        // 3. DB에서 사용자 정보 조회
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // 4. 저장된 리프레시 토큰 검증
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 5. 토큰 만료 확인
        if (jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new ExpectedException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 6. 새 액세스 토큰 생성
        String newAccessToken = jwtTokenUtil.generateAccessToken(user.getEmail());

        // 7. Authentication 반환
        return new TokenAuthentication(
                new AccountAdapter(user),
                newAccessToken,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TokenAuthentication.class.isAssignableFrom(authentication);
    }
}