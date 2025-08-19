package com.siso.user.infrastructure.oauth2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class AppleOAuthProviderClient implements OAuthProviderClient {
    @Override
    public Map<String, Object> getUserAttributes(String idToken) {
        // iOS에서 전달받은 id_token(JWT) 파싱
        Map<String, Object> attributes = new HashMap<>();

        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid Apple ID Token");
            }

            // JWT payload 부분(base64 decode)
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            attributes = objectMapper.readValue(payloadJson, new TypeReference<>() {});

            System.out.println("Decoded Apple ID Token Payload: " + attributes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Apple ID Token", e);
        }

        return attributes;
    }
}