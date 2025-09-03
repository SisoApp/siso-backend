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
 * S3 Presigned URL 생성 유틸리티 클래스
 * 
 * 클라이언트가 임시로 S3 객체에 접근할 수 있는 presigned URL을 생성합니다.
 * AWS 자격 증명 없이도 지정된 시간 동안 파일을 다운로드할 수 있습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3PresignedUrlUtil {

//    @Value("${aws.s3.bucket}") // 로컬용
    @Value("${cloud.aws.s3.bucket}") // 배포용
    private String bucket;

    private final S3Client s3Client;

    /**
     * 이미지 파일용 Presigned GET URL 생성
     * 
     * @param key S3 객체 키
     * @return presigned GET URL (5분 유효)
     */
    public String generatePresignedGetUrl(String key) {
        return generatePresignedGetUrl(key, Duration.ofMinutes(5));
    }

    /**
     * 이미지 파일용 Presigned GET URL 생성 (사용자 정의 만료 시간)
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

            log.info("Presigned GET URL 생성 성공 - key: {}, duration: {}분", 
                    key, duration.toMinutes());
            
            return presignedUrl;

        } catch (SdkClientException sce) {
            log.error("Presigned URL 생성 실패 - AWS 자격 증명 오류 - key: {}, message: {}", 
                    key, sce.getMessage(), sce);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "AWS 자격 증명 오류로 파일 접근 URL을 생성할 수 없습니다.");
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패 - 기타 오류 - key: {}, message: {}", 
                    key, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "파일 접근 URL 생성 중 알 수 없는 오류가 발생했습니다.");
        }
    }

    /**
     * 단기 접근용 Presigned GET URL 생성 (5분 유효)
     * 보안이 중요한 파일에 사용
     * 
     * @param key S3 객체 키
     * @return presigned GET URL (5분 유효)
     */
    public String generateShortTermPresignedGetUrl(String key) {
        return generatePresignedGetUrl(key, Duration.ofMinutes(5));
    }

    /**
     * 장기 접근용 Presigned GET URL 생성 (1시간 유효)
     * 공개 콘텐츠나 캐시가 필요한 파일에 사용
     * 
     * @param key S3 객체 키
     * @return presigned GET URL (1시간 유효)
     */
    public String generateLongTermPresignedGetUrl(String key) {
        return generatePresignedGetUrl(key, Duration.ofHours(1));
    }
}
