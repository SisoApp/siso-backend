package com.siso.call.application;

import io.agora.media.RtcTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgoraTokenService {
    @Value("${agora.appId}")
    private String appId;

    @Value("${agora.appCertificate}")
    private String appCertificate;

    private static final int TOKEN_EXPIRATION = 3600;

    // Token 발급
    public String generateToken(String channelName, Long userId) {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        return tokenBuilder.buildTokenWithUid(
                appId,
                appCertificate,
                channelName,
                userId.intValue(),
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                TOKEN_EXPIRATION,       // 토큰 자체 만료 시간 (1시간)
                TOKEN_EXPIRATION        // 권한 만료 시간 (1시간)
        );
    }
}