package com.siso.user.infrastructure.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class AppleOAuthProviderClient implements OAuthProviderClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Object> getUserAttributes(String authorizationCode, String codeVerifier) {
        // Apple은 JWT를 파싱해야 하므로, 임시 Map 반환
        // 실제 구현 시 JWT를 decode 후 Map<String, Object>로 변환
        String idToken = authorizationCode; // code 대신 id_token을 받을 수도 있음

        Map<String, Object> attributes = new HashMap<>();
        // JWT 디코딩 로직 필요
        // attributes.put("email", emailFromToken);
        // attributes.put("sub", subFromToken);

        return attributes;
    }
}