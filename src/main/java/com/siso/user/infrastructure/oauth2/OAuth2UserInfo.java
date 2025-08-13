package com.siso.user.infrastructure.oauth2;

import java.util.Map;

public interface OAuth2UserInfo {
    Map<String, Object> getAttributes();
    String getProviderId();
    String getProvider();
    String getPhoneNumber();
}