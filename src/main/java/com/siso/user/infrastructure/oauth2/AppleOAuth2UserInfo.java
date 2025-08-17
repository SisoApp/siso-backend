package com.siso.user.infrastructure.oauth2;

import java.util.Map;

public class AppleOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getPhoneNumber() {
        // 애플 로그인 시 phone_number는 signup 시점에만 제공될 수 있음
        if (attributes.containsKey("phone_number")) {
            return (String) attributes.get("phone_number");
        }
        return null;
    }
}
