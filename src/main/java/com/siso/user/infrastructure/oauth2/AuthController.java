package com.siso.user.infrastructure.oauth2;

import com.siso.user.dto.request.OAuthLoginRequestDto;
import com.siso.user.dto.response.TokenResponseDto;
import com.siso.user.infrastructure.jwt.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final OAuthService oAuthService;
    private final TokenService tokenService;

    @PostMapping("/kakao")
    public TokenResponseDto loginWithKakao(@RequestBody OAuthLoginRequestDto oAuthLoginRequestDto) {
        return oAuthService.loginWithProvider("kakao", oAuthLoginRequestDto.getAccessToken());
    }

    @PostMapping("/apple")
    public TokenResponseDto loginWithApple(@RequestBody OAuthLoginRequestDto oAuthLoginRequestDto) {
        return oAuthService.loginWithProvider("apple", oAuthLoginRequestDto.getAccessToken());
    }

    // Refresh Token으로 Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestHeader("Authorization") String refreshTokenHeader) {
        String refreshToken = refreshTokenHeader.replace("Bearer ", "");
        Map<String, Object> response = tokenService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
