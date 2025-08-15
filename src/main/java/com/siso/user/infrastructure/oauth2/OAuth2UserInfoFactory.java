package com.siso.user.infrastructure.oauth2;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.Provider;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(Provider.KAKAO.toString())) {
            return new KakaoOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(Provider.APPLE.toString())) {
            return new AppleOAuth2UserInfo(attributes);
        } else {
            throw new ExpectedException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
        }
    }
}