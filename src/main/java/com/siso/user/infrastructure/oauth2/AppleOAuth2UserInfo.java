package com.siso.user.infrastructure.oauth2;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class AppleOAuth2UserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProviderId() {
        return attributes.get("sub").toString();
    }

    @Override
    public String getProvider() {
        return "APPLE";
    }

    @Override
    public String getPhoneNumber() {
        // 애플은 전화번호를 제공하지 않으므로 null 반환
        return null;
    }
}