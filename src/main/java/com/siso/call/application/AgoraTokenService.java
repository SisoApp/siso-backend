package com.siso.call.application;

import io.agora.media.RtcTokenBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AgoraTokenService {
    @Value("${agora.appId}")
    private String appId;

    @Value("${agora.appCertificate}")
    private String appCertificate;

    // Token 발급
    public String generateToken(String channelName, int uid, int expireSeconds) throws Exception {
        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();
        RtcTokenBuilder.Role role = RtcTokenBuilder.Role.Role_Publisher;
        int currentTimestamp = (int) Instant.now().getEpochSecond();
        int privilegeExpireTs = currentTimestamp + expireSeconds;

        return tokenBuilder.buildTokenWithUid(appId, appCertificate, channelName, uid, role, privilegeExpireTs);
    }
}