package com.siso.user.infrastructure.jwt;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.application.UserProfileService;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.response.TokenResponseDto;
import com.siso.user.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final UserProfileService userProfileService;

    public Map<String, Object> refreshAccessToken(String oldRefreshToken) {
        // 1. RefreshToken 검증
        String email = jwtTokenUtil.getEmailFromToken(oldRefreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // DB 저장된 refreshToken과 비교
        if (!oldRefreshToken.equals(user.getRefreshToken())) {
            throw new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 토큰 타입 체크
        if (!jwtTokenUtil.isRefreshToken(oldRefreshToken)) {
            throw new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 만료 여부 확인
        if (jwtTokenUtil.isTokenExpired(oldRefreshToken)) {
            throw new ExpectedException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 2. 새 AccessToken + RefreshToken 발급
        String newAccessToken = jwtTokenUtil.generateAccessToken(email);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(email);

        // 3. DB에 RefreshToken 갱신
        user.updateRefreshToken(newRefreshToken);
        user.updateRegistrationStatus(RegistrationStatus.LOGIN);
        userRepository.save(user);

        // 4. TokenResponseDto 생성
        boolean hasProfile = userProfileService.existsByUserId(user.getId());
        TokenResponseDto tokenResponse = new TokenResponseDto(newAccessToken, newRefreshToken, user.getRegistrationStatus(), hasProfile);

        // 5. 응답 구조
        Map<String, Object> response = new HashMap<>();
//        response.put("accessToken", newAccessToken);
        response.put("token", tokenResponse); // tokenResponse는 TokenResponseDto → 새 RefreshToken + RegistrationStatus
        response.put("user", UserResponseDto.from(user));

        return response;
    }
}

