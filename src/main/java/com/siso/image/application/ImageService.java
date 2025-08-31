package com.siso.image.application;

import com.siso.image.domain.model.Image;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ImageService — S3 업로드/교체/삭제까지 일원화 (충돌 정리 완료)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    // === 의존성 주입 ===
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final UserValidationUtil userValidationUtil;
    private final ImageProperties imageProperties; // maxImagesPerUser 등 사용
    
    // S3 유틸리티 클래스들
    private final S3UploadUtil s3UploadUtil;
    private final S3DeleteUtil s3DeleteUtil;
    private final S3KeyUtil s3KeyUtil;
    private final S3PresignedUrlUtil s3PresignedUrlUtil;
    private final ImageCountValidationUtil imageCountValidationUtil;

    // ===================== 공개 API 메서드들 =====================

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    /** 다중 이미지 업로드 (S3) */
    @Transactional
    public List<ImageResponseDto> uploadMultipleImages(List<MultipartFile> files, ImageRequestDto request) {
        Long userId = request.getUserId();
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

            uploaded.add(ImageResponseDto.fromEntity(saved));
        }
        log.info("다중 이미지 업로드 완료 - 사용자: {}, 업로드된 파일 수: {}", userId, uploaded.size());
        return uploaded;
    }

    /** 특정 사용자의 이미지 목록 조회 */
    public List<ImageResponseDto> getImagesByUserId(Long userId) {
//        List<Image> images = imageRepository.findByUserIdOrderByCreatedAtAsc(userId);
//        return images.stream().map(ImageResponseDto::fromEntity).collect(Collectors.toList());

        return imageRepository.findByUserIdOrderByCreatedAtAsc(userId)
                .stream().map(ImageResponseDto::fromEntity).collect(Collectors.toList());

    }

    /** 이미지 단일 조회 */
    public ImageResponseDto getImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        return ImageResponseDto.fromEntity(image);
    }

    /** 이미지 수정 (S3 파일 교체) */
    @Transactional
    public ImageResponseDto updateImage(Long id, MultipartFile file, ImageRequestDto request) {
        Long userId = request.getUserId();
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
        log.info("이미지 파일 교체 완료 - id: {}, oldKey: {}, newKey: {}", id, oldKey, newKey);
        return ImageResponseDto.fromEntity(existing);
    }

    /** 이미지 삭제 (S3 + DB) */
    @Transactional
    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));

        String key = s3KeyUtil.extractKey(image.getPath());
        s3DeleteUtil.safeDeleteS3(key);

        imageRepository.delete(image);
        log.info("이미지 삭제 완료 - ID: {}, key: {}", id, key);
    }

    // ===================== Presigned URL 관련 메서드들 =====================

    /**
     * 이미지 Presigned GET URL 생성
     * 클라이언트가 임시로 이미지에 접근할 수 있는 URL을 생성합니다.
     * 
     * @param imageId 이미지 ID
     * @param userId 요청한 사용자 ID (소유권 검증용)
     * @return 15분간 유효한 presigned URL
     */
    public String getImagePresignedUrl(Long imageId, Long userId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        
        // 사용자 소유권 검증
        userValidationUtil.validateUserOwnership(image.getUser().getId(), userId);
        
        String key = s3KeyUtil.extractKey(image.getPath());
        String presignedUrl = s3PresignedUrlUtil.generatePresignedGetUrl(key);
        
        log.info("이미지 Presigned URL 생성 - imageId: {}, userId: {}", imageId, userId);
        return presignedUrl;
    }

    /**
     * 이미지 단기 Presigned GET URL 생성 (5분 유효)
     * 보안이 중요한 이미지나 빠른 미리보기용
     * 
     * @param imageId 이미지 ID
     * @param userId 요청한 사용자 ID (소유권 검증용)
     * @return 5분간 유효한 presigned URL
     */
    public String getImageShortTermPresignedUrl(Long imageId, Long userId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        
        // 사용자 소유권 검증
        userValidationUtil.validateUserOwnership(image.getUser().getId(), userId);
        
        String key = s3KeyUtil.extractKey(image.getPath());
        String presignedUrl = s3PresignedUrlUtil.generateShortTermPresignedGetUrl(key);
        
        log.info("이미지 단기 Presigned URL 생성 - imageId: {}, userId: {}", imageId, userId);
        return presignedUrl;
    }

    /**
     * 이미지 장기 Presigned GET URL 생성 (1시간 유효)
     * 공개 갤러리나 캐시가 필요한 이미지용
     * 
     * @param imageId 이미지 ID
     * @param userId 요청한 사용자 ID (소유권 검증용)
     * @return 1시간간 유효한 presigned URL
     */
    public String getImageLongTermPresignedUrl(Long imageId, Long userId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        
        // 사용자 소유권 검증
        userValidationUtil.validateUserOwnership(image.getUser().getId(), userId);
        
        String key = s3KeyUtil.extractKey(image.getPath());
        String presignedUrl = s3PresignedUrlUtil.generateLongTermPresignedGetUrl(key);
        
        log.info("이미지 장기 Presigned URL 생성 - imageId: {}, userId: {}", imageId, userId);
        return presignedUrl;
    }
}