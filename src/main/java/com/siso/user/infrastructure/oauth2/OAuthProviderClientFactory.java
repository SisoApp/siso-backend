package com.siso.user.infrastructure.oauth2;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthProviderClientFactory {
    private final KakaoOAuthProviderClient kakaoClient;
    private final AppleOAuthProviderClient appleClient;

    public OAuthProviderClient getClient(String providerName) {
        switch(providerName.toLowerCase()) {
            case "kakao": return kakaoClient;
            case "apple": return appleClient;
            default: throw new ExpectedException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
        }
    }
}
