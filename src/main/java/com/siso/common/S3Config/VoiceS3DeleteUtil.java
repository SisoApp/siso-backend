package com.siso.common.S3Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

/**
 * 음성 샘플 S3 파일 삭제 유틸리티 클래스
 * 
 * 음성 샘플을 S3에서 삭제하는 공통 기능을 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceS3DeleteUtil {


    //배포용
    // @Value("${cloud.aws.s3.bucket}")
    // private String bucket;

    //로컬
    @Value("${aws.s3.bucket}")
    private String bucket;
//

    private final S3Client s3Client;

    /**
     * S3에서 음성 파일 안전 삭제 (실패해도 예외를 던지지 않음)
     * 
     * @param key 삭제할 S3 객체 키
     */
    public void safeDeleteS3(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            log.info("S3 음성 파일 삭제 완료 - key: {}", key);
        } catch (Exception e) {
            log.warn("S3 음성 파일 삭제 실패(무시) - key: {}", key, e);
        }
    }
}
