package com.siso.call.application;

import io.agora.media.RtcTokenBuilder2;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgoraTokenService {

    @Value("${agora.app.id}")
    private String appId;

    @Value("${agora.app.certificate}")
    private String appCertificate;

    private static final int TOKEN_EXPIRATION = 3600;

    @PostConstruct
    public void printAgoraConfig() {
        log.info("Agora AppId = {}", appId);
        log.info("Agora AppCertificate = {}", appCertificate != null && !appCertificate.isEmpty() ? "****(set)" : "(empty)");
    }

    // Token 발급
    public String generateToken(String channelName) {
        log.debug("Generating token with appId={}, appCertificate set={}, channelName={}",
                appId,
                appCertificate != null && !appCertificate.isEmpty(),
                channelName);

        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        String token = tokenBuilder.buildTokenWithUid(
                appId,
                appCertificate,
                channelName,
                0,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                TOKEN_EXPIRATION,
                TOKEN_EXPIRATION
        );

        log.debug("Generated token={}", token);
        return token;
    }
}