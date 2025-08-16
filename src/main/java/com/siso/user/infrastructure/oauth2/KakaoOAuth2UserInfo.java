package com.siso.user.infrastructure.oauth2;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;
    private Map<String, Object> kakaoAccount;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getPhoneNumber() {
        // 전화번호 파싱 로직 추가 (카카오 API 응답 형태에 따라 수정 필요)
        if (kakaoAccount.containsKey("phone_number")) {
            return (String) kakaoAccount.get("phone_number");
        }
        return null;
    }
}