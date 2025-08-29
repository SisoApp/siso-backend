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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ImageService — S3 업로드/교체/삭제까지 일원화 (충돌 정리 완료)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // === 의존성 주입 ===
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final UserValidationUtil userValidationUtil;
    private final ImageProperties imageProperties; // maxImagesPerUser 등 사용
    private final S3Client s3Client;

    private static final String PREFIX = "images/"; // S3 폴더 prefix

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
            throw new ExpectedException(ErrorCode.INVALID_IMAGE_FILE);
        }
        validateImageCountLimit(userId, files.size());

        List<ImageResponseDto> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFileNotEmpty(file);

            String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
            String serverFileName = uuidName(originalName);
            String key = buildKey(/*withUser*/ false ? userId : null, serverFileName);
            String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

            putObject(key, file, contentType);

            user.addImage(s3Url(key), serverFileName, originalName);

            Image saved = imageRepository.findByServerImageName(serverFileName)
                    .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
            saved.setPath(s3Url(key));

            uploaded.add(ImageResponseDto.fromEntity(saved));
        }
        log.info("다중 이미지 업로드 완료 - 사용자: {}, 업로드된 파일 수: {}", userId, uploaded.size());
        return uploaded;
    }

    /** 특정 사용자의 이미지 목록 조회 */
    public List<ImageResponseDto> getImagesByUserId(Long userId) {
        List<Image> images = imageRepository.findByUserIdOrderByCreatedAtAsc(userId);
        return images.stream().map(ImageResponseDto::fromEntity).collect(Collectors.toList());
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
        String oldKey = extractKey(existing.getPath());
        safeDeleteS3(oldKey);

        // 새 업로드
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String serverFileName = uuidName(originalName);
        String newKey = buildKey(/*withUser*/ false ? userId : null, serverFileName);
        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

        putObject(newKey, file, contentType);

        existing.updateImage(s3Url(newKey), serverFileName, originalName);
        log.info("이미지 파일 교체 완료 - id: {}, oldKey: {}, newKey: {}", id, oldKey, newKey);
        return ImageResponseDto.fromEntity(existing);
    }

    /** 이미지 삭제 (S3 + DB) */
    @Transactional
    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));

        String key = extractKey(image.getPath());
        safeDeleteS3(key);

        imageRepository.delete(image);
        log.info("이미지 삭제 완료 - ID: {}, key: {}", id, key);
    }

    // ===================== 내부 유틸 =====================

    private void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ExpectedException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    private void validateImageCountLimit(Long userId, int willUploadCount) {
        long current = imageRepository.countByUserId(userId);
        int max = imageProperties.getMaxImagesPerUser();
        if (current + willUploadCount > max) {
            throw new ExpectedException(ErrorCode.IMAGE_MAX_COUNT_EXCEEDED);
        }
    }

    private String uuidName(String originalName) {
        String safe = Optional.ofNullable(originalName).orElse("file").replaceAll("[^a-zA-Z0-9._-]", "_");
        return UUID.randomUUID() + "-" + safe;
    }

    /** userId 하위로 폴더를 나누고 싶다면 withUser=true로 바꿔 사용 */
    private String buildKey(Long userId, String serverFileName) {
        if (userId != null) {
            return PREFIX + userId + "/" + serverFileName;
        }
        return PREFIX + serverFileName;
    }

    private void putObject(String key, MultipartFile file, String contentType) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (Exception e) {
            log.error("S3 업로드 실패 - key: {}", key, e);
            throw new ExpectedException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    private void safeDeleteS3(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception e) {
            log.warn("S3 삭제 실패(무시) - key: {}", key, e);
        }
    }

    private String extractKey(String keyOrUrl) {
        if (keyOrUrl == null) return null;
        // 이미 key라면 그대로 리턴
        if (!keyOrUrl.startsWith("http")) return keyOrUrl;
        // https://{bucket}.s3.{region}.amazonaws.com/{key}
        int idx = keyOrUrl.indexOf(".amazonaws.com/");
        if (idx == -1) return keyOrUrl; // 예상치 못한 포맷이면 원본 반환
        return keyOrUrl.substring(idx + ".amazonaws.com/".length());
    }

    private String s3Url(String key) {
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + key;
    }
}
