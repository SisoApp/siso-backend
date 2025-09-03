package com.siso.common.S3Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

/**
 * 음성 샘플 S3 파일 업로드 유틸리티 클래스
 * 
 * 음성 샘플을 S3에 업로드하는 공통 기능을 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceS3UploadUtil {

//    @Value("${aws.s3.bucket}") // 로컬용
    @Value("${cloud.aws.s3.bucket}") // 배포용
    private String bucket;

    private final S3Client s3Client;

    /**
     * S3에 음성 파일 업로드
     * 
     * @param key S3 객체 키
     * @param file 업로드할 음성 파일
     * @param contentType 파일의 Content-Type
     */
    public void putObject(String key, MultipartFile file, String contentType) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (SdkClientException sce) {
            log.error("S3 음성 파일 업로드 실패 - AWS 자격 증명 오류 - key: {}, message: {}", key, sce.getMessage(), sce);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "AWS 자격 증명 오류로 음성 파일을 업로드할 수 없습니다.");
        } catch (IOException ioe) {
            log.error("S3 음성 파일 업로드 실패 - 파일 읽기 오류 - key: {}, message: {}", key, ioe.getMessage(), ioe);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "음성 파일을 읽는 데 실패했습니다.");
        } catch (Exception e) {
            log.error("S3 음성 파일 업로드 실패 - 기타 오류 - key: {}, message: {}", key, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "음성 파일 업로드 중 알 수 없는 오류가 발생했습니다.");
        }
    }

    /**
     * S3 URL 생성
     * 
     * @param key S3 객체 키
     * @return S3 URL
     */
    public String generateS3Url(String key) {
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + key;
    }
}
