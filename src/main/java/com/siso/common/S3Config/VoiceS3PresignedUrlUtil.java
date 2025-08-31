package com.siso.common.S3Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

/**
 * 음성 파일용 S3 Presigned URL 생성 유틸리티 클래스
 * 
 * 클라이언트가 임시로 음성 파일에 접근할 수 있는 presigned URL을 생성합니다.
 * AWS 자격 증명 없이도 지정된 시간 동안 음성 파일을 재생하거나 다운로드할 수 있습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceS3PresignedUrlUtil {

//    @Value("${aws.s3.bucket}") // 로컬용
    @Value("${cloud.aws.s3.bucket}") // 배포용
    private String bucket;

    private final S3Client s3Client;

    /**
     * 음성 파일용 Presigned GET URL 생성
     * 기본 10분 유효 (음성 재생 시간을 고려)
     * 
     * @param key S3 객체 키
     * @return presigned GET URL (10분 유효)
     */
    public String generatePresignedGetUrl(String key) {
        return generatePresignedGetUrl(key, Duration.ofMinutes(10));
    }

    /**
     * 음성 파일용 Presigned GET URL 생성 (사용자 정의 만료 시간)
     * 
     * @param key S3 객체 키
     * @param duration URL 유효 기간
     * @return presigned GET URL
     */
    public String generatePresignedGetUrl(String key, Duration duration) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(s3Client.serviceClientConfiguration().region())
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);
            String presignedUrl = presignedGetObjectRequest.url().toString();

            log.info("음성 파일 Presigned GET URL 생성 성공 - key: {}, duration: {}분", 
                    key, duration.toMinutes());
            
            return presignedUrl;

        } catch (SdkClientException sce) {
            log.error("음성 파일 Presigned URL 생성 실패 - AWS 자격 증명 오류 - key: {}, message: {}", 
                    key, sce.getMessage(), sce);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "AWS 자격 증명 오류로 음성 파일 접근 URL을 생성할 수 없습니다.");
        } catch (Exception e) {
            log.error("음성 파일 Presigned URL 생성 실패 - 기타 오류 - key: {}, message: {}", 
                    key, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "음성 파일 접근 URL 생성 중 알 수 없는 오류가 발생했습니다.");
        }
    }

    /**
     * 단기 재생용 Presigned GET URL 생성 (3분 유효)
     * 빠른 미리보기나 짧은 재생에 사용
     * 
     * @param key S3 객체 키
     * @return presigned GET URL (3분 유효)
     */
    public String generateShortPlayPresignedGetUrl(String key) {
        return generatePresignedGetUrl(key, Duration.ofMinutes(3));
    }
}
