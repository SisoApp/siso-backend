package com.siso.common.S3Config;

import com.siso.image.domain.model.Image;
import com.siso.image.domain.model.PresignedUrlType;
import com.siso.image.domain.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Presigned URL 자동 관리 유틸리티 클래스
 * 
 * 이미지의 Presigned URL을 자동으로 생성, 갱신, 관리하는 기능을 제공합니다.
 * ImageService와 ImageTestService에서 공통으로 사용됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresignedUrlManagementUtil {

    private final ImageRepository imageRepository;
    private final S3PresignedUrlUtil s3PresignedUrlUtil;
    private final S3KeyUtil s3KeyUtil;

    /**
     * Presigned URL 자동 생성 및 저장
     * 
     * @param image 이미지 엔티티
     * @param urlType Presigned URL 타입
     */
    @Transactional
    public void generateAndSavePresignedUrl(Image image, PresignedUrlType urlType) {
        try {
            String key = s3KeyUtil.extractKey(image.getPath());
            String presignedUrl = s3PresignedUrlUtil.generatePresignedGetUrl(key);
            
            // 더 정확한 만료 시간 생성 (밀리초 단위까지 고려)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(5);
            
            image.updatePresignedUrl(presignedUrl, expiresAt, urlType);
            imageRepository.save(image);
            
            log.info("Presigned URL 자동 생성 완료 - imageId: {}, type: {}, 생성시간: {}, 만료시간: {}, 유효시간: 5분", 
                    image.getId(), urlType, now, expiresAt);
                    
        } catch (Exception e) {
            log.error("Presigned URL 자동 생성 실패 - imageId: {}, message: {}", 
                    image.getId(), e.getMessage(), e);
            // Presigned URL 생성 실패해도 이미지 조회는 가능하도록 예외를 던지지 않음
        }
    }

    /**
     * 만료된 Presigned URL들을 일괄 갱신
     */
    @Transactional
    public void refreshExpiredPresignedUrls() {
        List<Image> expiredImages = imageRepository.findAll().stream()
                .filter(image -> !image.isPresignedUrlValid())
                .collect(Collectors.toList());
        
        for (Image image : expiredImages) {
            generateAndSavePresignedUrl(image, PresignedUrlType.DEFAULT);
        }
        
        log.info("만료된 Presigned URL 일괄 갱신 완료 - 갱신된 이미지 수: {}", expiredImages.size());
    }

    /**
     * 특정 사용자의 만료된 Presigned URL들을 일괄 갱신
     * 
     * @param userId Presigned URL을 갱신할 사용자 ID
     * @return 갱신된 이미지 수
     */
    @Transactional
    public int refreshExpiredPresignedUrlsByUserId(Long userId) {
        List<Image> userImages = imageRepository.findByUserIdOrderByCreatedAtAsc(userId);
        int refreshedCount = 0;
        
        for (Image image : userImages) {
            if (!image.isPresignedUrlValid()) {
                generateAndSavePresignedUrl(image, PresignedUrlType.DEFAULT);
                refreshedCount++;
            }
        }
        
        log.info("사용자 {}의 만료된 Presigned URL 일괄 갱신 완료 - 갱신된 이미지 수: {}", userId, refreshedCount);
        return refreshedCount;
    }

    /**
     * 테스트용 특정 사용자의 만료된 Presigned URL들을 일괄 갱신
     * 
     * @param userId Presigned URL을 갱신할 사용자 ID
     * @return 갱신된 이미지 수
     */
    @Transactional
    public int refreshExpiredPresignedUrlsByUserIdForTest(Long userId) {
        log.info("=== Presigned URL 관리 유틸리티 - 테스트용 갱신 시작 ===");
        log.info("입력 userId: {}", userId);
        
        try {
            log.info("사용자 이미지 조회 시작...");
            List<Image> userImages = imageRepository.findByUserIdOrderByCreatedAtAsc(userId);
            log.info("사용자 이미지 조회 완료. 이미지 개수: {}", userImages.size());
            
            int refreshedCount = 0;
            
            for (Image image : userImages) {
                log.info("이미지 ID: {}, Presigned URL 유효성: {}", image.getId(), image.isPresignedUrlValid());
                if (!image.isPresignedUrlValid()) {
                    log.info("이미지 ID {}의 Presigned URL 갱신 시작...", image.getId());
                    generateAndSavePresignedUrl(image, PresignedUrlType.DEFAULT);
                    refreshedCount++;
                    log.info("이미지 ID {}의 Presigned URL 갱신 완료", image.getId());
                }
            }
            
            log.info("전체 갱신 완료. 갱신된 이미지 수: {}", refreshedCount);
            log.info("테스트용 사용자 {}의 만료된 Presigned URL 일괄 갱신 완료 - 갱신된 이미지 수: {}", userId, refreshedCount);
            return refreshedCount;
            
        } catch (Exception e) {
            log.error("=== Presigned URL 관리 유틸리티에서 오류 발생 ===");
            log.error("오류 타입: {}", e.getClass().getSimpleName());
            log.error("오류 메시지: {}", e.getMessage());
            log.error("스택 트레이스:", e);
            throw e;
        }
    }
}
