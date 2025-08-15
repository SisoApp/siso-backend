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
    public String getId() {
        return (String) attributes.get("sub"); // 'sub' 클레임이 Apple의 고유 ID입니다.
    }

    @Override
    public String getPhoneNumber() {
        // Apple은 'phone_number' 스코프를 통해 전화번호를 제공할 수 있습니다.
        // 실제 구현 시 제공되는 클레임에 따라 로직을 수정해야 합니다.
        return (String) attributes.get("phone_number");
    }
}