package com.siso.common.firebase.infrastructure;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Firebase 초기화 설정 클래스
 * 
 * Spring Boot 애플리케이션 시작 시 Firebase Admin SDK를 초기화합니다.
 * Service Account 키 파일을 사용하여 Firebase 프로젝트와 연결하고,
 * FCM(Firebase Cloud Messaging) 서비스를 사용할 수 있도록 설정합니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    /**
     * Firebase Service Account 키 파일 경로
     * application.yml에서 firebase.service-account 속성으로 설정
     */
    @Value("${firebase.service-account}")
    private Resource serviceAccount;

    /**
     * Firebase Admin SDK 초기화
     * 
     * 애플리케이션 시작 시 자동으로 실행되어 Firebase를 초기화합니다.
     * 중복 초기화를 방지하기 위해 FirebaseApp.getApps()로 기존 앱 확인 후 초기화합니다.
     * 
     * @throws IOException Service Account 파일 읽기 실패 시 발생
     */
    @PostConstruct
    public void init() throws IOException {
        // Firebase 앱이 이미 초기화되어 있는지 확인
        if (FirebaseApp.getApps().isEmpty()) {
            // Service Account 키 파일을 사용하여 Firebase 옵션 설정
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                    .build();
            
            // Firebase 앱 초기화
            FirebaseApp.initializeApp(options);
        }
    }
}