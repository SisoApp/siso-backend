package com.siso.user.infrastructure.oauth2;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.Provider;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        try {
            Provider provider = Provider.valueOf(registrationId.toUpperCase());
            switch(provider) {
                case KAKAO: return new KakaoOAuth2UserInfo(attributes);
                case APPLE: return new AppleOAuth2UserInfo(attributes);
                default: throw new ExpectedException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
            }
        } catch(IllegalArgumentException e) {
            throw new ExpectedException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
        }
    }
}