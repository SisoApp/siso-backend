package com.siso.user.infrastructure.oauth2;

import com.nimbusds.oauth2.sdk.TokenResponse;
import com.siso.user.dto.request.OAuthLoginRequestDto;
import com.siso.user.dto.request.RefreshTokenRequestDto;
import com.siso.user.dto.response.TokenResponseDto;
import com.siso.user.infrastructure.jwt.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final OAuthService oAuthService;
    private final TokenService tokenService;

    // 브라우저 테스트용 GET callback
    @GetMapping("/kakao/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam String code) {
        // 브라우저에서 Authorization Code 확인
        return ResponseEntity.ok("Authorization Code: " + code);
    }

    @PostMapping("/kakao")
    public TokenResponseDto loginWithKakao(@RequestBody OAuthLoginRequestDto oAuthLoginRequestDto) {
        return oAuthService.loginWithProvider("kakao", oAuthLoginRequestDto.getAccessToken(), oAuthLoginRequestDto.getCodeVerifier());
    }

    @PostMapping("/apple")
    public TokenResponseDto loginWithApple(@RequestBody OAuthLoginRequestDto oAuthLoginRequestDto) {
        return oAuthService.loginWithProvider("apple", oAuthLoginRequestDto.getAccessToken(), oAuthLoginRequestDto.getCodeVerifier());
    }

    // Refresh Token으로 Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");
        return ResponseEntity.ok(tokenService.refreshAccessToken(token));
    }
}
