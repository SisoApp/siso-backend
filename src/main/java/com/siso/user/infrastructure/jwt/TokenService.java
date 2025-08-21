package com.siso.user.infrastructure.jwt;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.RegistrationStatus;
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
        // 1. DB에서 사용자 조회
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 2. 새 AccessToken 발급
        String newAccessToken = jwtTokenUtil.generateAccessToken(user.getEmail());

        // 3. 새 RefreshToken 발급 및 DB 저장
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(newRefreshToken);
        userRepository.save(user);

        // 4. DTO 반환 (registrationStatus: 로그인)
        return new TokenResponseDto(newRefreshToken, RegistrationStatus.LOGIN);
    }
}
