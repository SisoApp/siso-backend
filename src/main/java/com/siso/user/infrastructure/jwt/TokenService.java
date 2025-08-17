package com.siso.user.infrastructure.jwt;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.response.TokenResponseDto;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public TokenResponseDto refreshAccessToken(String refreshToken) {
        try {
            jwtTokenUtil.validateToken(refreshToken); // 서명 위조, 형식 에러 체크
            if (jwtTokenUtil.isTokenExpired(refreshToken)) {
                throw new ExpectedException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }
        } catch (JwtException e) {
            throw new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 1. DB에서 refreshToken으로 사용자 조회
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN));
        // 2. 새 Access Token 발급
        String newAccessToken = jwtTokenUtil.generateAccessToken(user.getEmail());
        // 3. 새 Refresh Token 발급 (이메일 포함)
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
}
