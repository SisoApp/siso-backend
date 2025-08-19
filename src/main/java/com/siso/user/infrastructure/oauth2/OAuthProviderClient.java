package com.siso.user.infrastructure.oauth2;

import java.util.Map;

public interface OAuthProviderClient {
    Map<String, Object> getUserAttributes(String accessToken);
}
