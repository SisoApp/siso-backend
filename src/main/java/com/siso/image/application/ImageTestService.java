package com.siso.image.application;

import com.siso.image.domain.model.Image;
import com.siso.image.domain.model.PresignedUrlType;
import com.siso.image.domain.repository.ImageRepository;
import com.siso.image.dto.request.ImageRequestDto;
import com.siso.image.dto.response.ImageResponseDto;
import com.siso.image.infrastructure.properties.ImageProperties;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.common.util.UserValidationUtil;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.common.S3Config.S3UploadUtil;
import com.siso.common.S3Config.S3DeleteUtil;
import com.siso.common.S3Config.S3KeyUtil;
import com.siso.common.S3Config.S3PresignedUrlUtil;
import com.siso.common.S3Config.ImageCountValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ImageTestService — 테스트용 이미지 서비스
 * 
 * 테스트 환경에서 사용할 수 있는 이미지 관련 기능을 제공합니다.
 * @CurrentUser 없이 userId를 직접 받아서 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageTestService {

    // === 의존성 주입 ===
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final UserValidationUtil userValidationUtil;
    private final ImageProperties imageProperties;
    
    // S3 유틸리티 클래스들
    private final S3UploadUtil s3UploadUtil;
    private final S3DeleteUtil s3DeleteUtil;
    private final S3KeyUtil s3KeyUtil;
    private final S3PresignedUrlUtil s3PresignedUrlUtil;
    private final ImageCountValidationUtil imageCountValidationUtil;

    // ===================== 테스트용 API 메서드들 =====================

    /**
     * 간단한 테스트용 메서드 (의존성 문제 확인용)
     */
    public String testSimple() {
        log.info("=== ImageTestService.testSimple() 호출됨 ===");
        return "ImageTestService 정상 작동!";
    }

    public User findById(Long userId) {
        log.info("=== ImageTestService.findById() 호출됨 - userId: {} ===", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    /** 테스트용 다중 이미지 업로드 (S3) + Presigned URL 자동 생성 */
    @Transactional
    public List<ImageResponseDto> uploadMultipleImagesForTest(List<MultipartFile> files, Long userId) {
        User user = findById(userId);

        userValidationUtil.validateUserExists(userId);
        if (files == null || files.isEmpty()) {
            throw new ExpectedException(ErrorCode.IMAGE_EMPTY);
        }
        imageCountValidationUtil.validateImageCountLimit(userId, files.size());

        List<ImageResponseDto> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new ExpectedException(ErrorCode.IMAGE_EMPTY);
            }

            String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
            String serverFileName = s3KeyUtil.generateUuidName(originalName);
            String key = s3KeyUtil.buildKey(userId, serverFileName);
            String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

            s3UploadUtil.putObject(key, file, contentType);

            user.addImage(s3UploadUtil.generateS3Url(key), serverFileName, originalName);

            Image saved = imageRepository.findByServerImageName(serverFileName)
                    .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_UPLOAD_PERSIST_FAIL));
            saved.setPath(s3UploadUtil.generateS3Url(key));

            // Presigned URL 자동 생성 및 저장
            generateAndSavePresignedUrl(saved, PresignedUrlType.DEFAULT);

            uploaded.add(ImageResponseDto.fromEntity(saved));
        }
        log.info("테스트용 다중 이미지 업로드 완료 - 사용자: {}, 업로드된 파일 수: {}", userId, uploaded.size());
        return uploaded;
    }

    /** 테스트용 특정 사용자의 이미지 목록 조회 (Presigned URL 포함) */
    public List<ImageResponseDto> getImagesByUserIdForTest(Long userId) {
        List<Image> images = imageRepository.findByUserIdOrderByCreatedAtAsc(userId);
        
        return images.stream()
                .map(image -> {
                    // Presigned URL이 없거나 만료된 경우 자동으로 새로 생성
                    if (!image.isPresignedUrlValid()) {
                        generateAndSavePresignedUrl(image, PresignedUrlType.DEFAULT);
                    }
                    return ImageResponseDto.fromEntity(image);
                })
                .collect(Collectors.toList());
    }

    /** 테스트용 특정 사용자의 이미지 목록 조회 (Presigned URL만 포함, 경량화) */
    public List<ImageResponseDto> getImagesByUserIdLightweightForTest(Long userId) {
        List<Image> images = imageRepository.findByUserIdOrderByCreatedAtAsc(userId);
        
        return images.stream()
                .map(image -> {
                    // Presigned URL이 없거나 만료된 경우 자동으로 새로 생성
                    if (!image.isPresignedUrlValid()) {
                        generateAndSavePresignedUrl(image, PresignedUrlType.DEFAULT);
                    }
                    
                    // 경량화된 DTO 생성 (기본 정보 + Presigned URL만)
                    return ImageResponseDto.builder()
                            .id(image.getId())
                            .userId(image.getUser().getId())
                            .presignedUrl(image.getPresignedUrl())
                            .presignedUrlExpiresAt(image.getPresignedUrlExpiresAt())
                            .presignedUrlType(image.getPresignedUrlType() != null ? image.getPresignedUrlType().name() : null)
                            .presignedUrlValid(image.isPresignedUrlValid())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** 테스트용 이미지 단일 조회 (Presigned URL 포함) */
    public ImageResponseDto getImageForTest(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        
        // Presigned URL이 없거나 만료된 경우 자동으로 새로 생성
        if (!image.isPresignedUrlValid()) {
            generateAndSavePresignedUrl(image, PresignedUrlType.DEFAULT);
        }
        
        return ImageResponseDto.fromEntity(image);
    }

    /** 테스트용 이미지 수정 (S3 파일 교체) + Presigned URL 재생성 */
    @Transactional
    public ImageResponseDto updateImageForTest(Long id, MultipartFile file, Long userId) {
        Image existing = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));

        userValidationUtil.validateUserOwnership(existing.getUser().getId(), userId);
        if (file == null || file.isEmpty()) {
            return ImageResponseDto.fromEntity(existing); // 파일 없으면 메타 변경 없음
        }

        // 기존 S3 삭제
        String oldKey = s3KeyUtil.extractKey(existing.getPath());
        s3DeleteUtil.safeDeleteS3(oldKey);

        // 새 업로드
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String serverFileName = s3KeyUtil.generateUuidName(originalName);
        String newKey = s3KeyUtil.buildKey(userId, serverFileName);
        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

        s3UploadUtil.putObject(newKey, file, contentType);

        existing.updateImage(s3UploadUtil.generateS3Url(newKey), serverFileName, originalName);
        
        // Presigned URL 재생성
        generateAndSavePresignedUrl(existing, PresignedUrlType.DEFAULT);
        
        log.info("테스트용 이미지 파일 교체 완료 - id: {}, oldKey: {}, newKey: {}", id, oldKey, newKey);
        return ImageResponseDto.fromEntity(existing);
    }

    /** 테스트용 이미지 삭제 (S3 + DB) */
    @Transactional
    public void deleteImageForTest(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));

        String key = s3KeyUtil.extractKey(image.getPath());
        s3DeleteUtil.safeDeleteS3(key);

        imageRepository.delete(image);
        log.info("테스트용 이미지 삭제 완료 - ID: {}, key: {}", id, key);
    }

    // ===================== Presigned URL 자동 관리 메서드들 =====================

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
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
            
            image.updatePresignedUrl(presignedUrl, expiresAt, urlType);
            imageRepository.save(image);
            
            log.info("테스트용 Presigned URL 자동 생성 완료 - imageId: {}, type: {}, expiresAt: {}", 
                    image.getId(), urlType, expiresAt);
                    
        } catch (Exception e) {
            log.error("테스트용 Presigned URL 자동 생성 실패 - imageId: {}, message: {}", 
                    image.getId(), e.getMessage(), e);
            // Presigned URL 생성 실패해도 이미지 조회는 가능하도록 예외를 던지지 않음
        }
    }

    /**
     * 테스트용 특정 사용자의 만료된 Presigned URL들을 일괄 갱신
     * 
     * @param userId Presigned URL을 갱신할 사용자 ID
     * @return 갱신된 이미지 수
     */
    @Transactional
    public int refreshExpiredPresignedUrlsByUserIdForTest(Long userId) {
        log.info("=== ImageTestService.refreshExpiredPresignedUrlsByUserIdForTest 시작 ===");
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
            log.error("=== ImageTestService에서 오류 발생 ===");
            log.error("오류 타입: {}", e.getClass().getSimpleName());
            log.error("오류 메시지: {}", e.getMessage());
            log.error("스택 트레이스:", e);
            throw e;
        }
    }
}
