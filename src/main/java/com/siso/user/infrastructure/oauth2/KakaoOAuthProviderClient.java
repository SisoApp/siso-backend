package com.siso.user.infrastructure.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class KakaoOAuthProviderClient implements OAuthProviderClient {
    private final KakaoProperties kakaoProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public KakaoOAuthProviderClient(@Qualifier("kakaoProperties") KakaoProperties kakaoProperties) {
        this.kakaoProperties = kakaoProperties;
    }

    @Override
    public Map<String, Object> getUserAttributes(String code, String codeVerifier) {
        // 1. AccessToken 교환
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoProperties.getClientId());
        body.add("redirect_uri", kakaoProperties.getRedirectUri());
        body.add("code", code);
        body.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. 사용자 정보 조회
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                userRequest,
                new ParameterizedTypeReference<>() {}
        );

        return userResponse.getBody();
    }
}
