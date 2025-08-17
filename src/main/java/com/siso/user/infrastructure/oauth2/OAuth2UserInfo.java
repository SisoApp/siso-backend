package com.siso.user.infrastructure.oauth2;

import java.util.Map;

public interface OAuth2UserInfo {
    Map<String, Object> getAttributes();
    String getId();
    String getEmail();
    String getPhoneNumber(); // 새로 추가
}
