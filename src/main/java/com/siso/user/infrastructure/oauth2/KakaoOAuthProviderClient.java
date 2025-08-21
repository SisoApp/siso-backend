package com.siso.user.infrastructure.oauth2;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class KakaoOAuthProviderClient implements OAuthProviderClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Object> getUserAttributes(String accessToken) {
        System.out.println("Received Kakao AccessToken from Mobile: " + accessToken);

        // 카카오 사용자 정보 조회
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken); // 카카오 SDK가 발급해준 AccessToken
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        try {
            ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    userRequest,
                    new ParameterizedTypeReference<>() {}
            );
            return userResponse.getBody();
        } catch (HttpClientErrorException e) {
            throw new ExpectedException(ErrorCode.OAUTH2_TOKEN_INVALID);
        }
    }
}

