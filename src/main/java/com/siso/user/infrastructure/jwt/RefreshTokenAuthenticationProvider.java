package com.siso.user.infrastructure.jwt;

import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.infrastructure.authentication.AccountAdapter;
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
        // 필터에서 전달받은 principal과 credentials를 올바르게 캐스팅
        String phoneNumber = (String) authentication.getPrincipal();
        String refreshToken = (String) authentication.getCredentials();

        // 1. DB에서 사용자 정보와 리프레시 토큰 조회 (전화번호로 조회)
        User user = userRepository.findActiveUserByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BadCredentialsException("사용자를 찾을 수 없습니다."));

        String foundRefreshToken = user.getRefreshToken();

        // 2. 저장된 리프레시 토큰과 클라이언트가 보낸 토큰 일치 여부 확인
        if (foundRefreshToken == null || !foundRefreshToken.equals(refreshToken)) {
            throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 3. 토큰의 만료 여부 확인 (이전의 JwtTokenUtil에서 로직이 개선되었으므로 사용 가능)
        if (jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new RefreshTokenExpiredException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
        }

        // 4. 새로운 액세스 토큰 생성
        String newAccessToken = jwtTokenUtil.generateAccessToken(user.getPhoneNumber());

        // 5. 새로운 Authentication 객체에 사용자 정보와 권한을 담아 반환
        return new TokenAuthentication(new AccountAdapter(user), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return TokenAuthentication.class.isAssignableFrom(authentication);
    }
}