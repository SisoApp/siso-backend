// src/main/java/.../image/application/ImageService.java
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final UserValidationUtil userValidationUtil;
    private final ImageProperties imageProperties;
    private final S3Client s3Client;

    private static final String PREFIX = "images/profiles/";

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    /** 단일 업로드 */
    @Transactional
    public ImageResponseDto uploadImage(MultipartFile file, ImageRequestDto request) {
        Long userId = request.getUserId();
        User user = findById(userId);

        userValidationUtil.validateUserExists(userId);
        validateFileNotEmpty(file);
        validateImageCountLimit(userId, 1);

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String serverFileName = uuidName(originalName);
        String key = buildKey(null, serverFileName); // 사용자별 폴더 원하면 userId 전달
        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

        putObject(key, file, contentType);

        user.addImage(s3Url(key), serverFileName, originalName);

        Image savedImage = imageRepository.findByServerImageName(serverFileName)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        savedImage.setPath(s3Url(key));

        log.info("이미지 업로드 완료 - ID: {}, userId: {}, key: {}", savedImage.getId(), userId, key);
        return ImageResponseDto.fromEntity(savedImage);
    }

    /** 다중 업로드 */
    @Transactional
    public List<ImageResponseDto> uploadMultipleImages(List<MultipartFile> files, ImageRequestDto request) {
        Long userId = request.getUserId();
        User user = findById(userId);

        userValidationUtil.validateUserExists(userId);
        if (files == null || files.isEmpty()) throw new ExpectedException(ErrorCode.INVALID_IMAGE_FILE);
        validateImageCountLimit(userId, files.size());

        List<ImageResponseDto> out = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFileNotEmpty(file);

            String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
            String serverFileName = uuidName(originalName);
            String key = buildKey(null, serverFileName);
            String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

            putObject(key, file, contentType);

            user.addImage(s3Url(key), serverFileName, originalName);

            Image saved = imageRepository.findByServerImageName(serverFileName)
                    .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
            saved.setPath(s3Url(key));

            out.add(ImageResponseDto.fromEntity(saved));
        }
        log.info("다중 업로드 완료 - userId: {}, count: {}", userId, out.size());
        return out;
    }

    public List<ImageResponseDto> getImagesByUserId(Long userId) {
        return imageRepository.findByUserIdOrderByCreatedAtAsc(userId)
                .stream().map(ImageResponseDto::fromEntity).collect(Collectors.toList());
    }

    public ImageResponseDto getImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        return ImageResponseDto.fromEntity(image);
    }

    /** 교체(수정) */
    @Transactional
    public ImageResponseDto updateImage(Long id, MultipartFile file, ImageRequestDto request) {
        Long userId = request.getUserId();
        Image existing = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));

        userValidationUtil.validateUserOwnership(existing.getUser().getId(), userId);
        if (file == null || file.isEmpty()) return ImageResponseDto.fromEntity(existing);

        String oldKey = extractKey(existing.getPath());
        safeDeleteS3(oldKey);

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String serverFileName = uuidName(originalName);
        String newKey = buildKey(null, serverFileName);
        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

        putObject(newKey, file, contentType);

        existing.updateImage(s3Url(newKey), serverFileName, originalName);
        log.info("이미지 교체 완료 - id: {}, oldKey: {}, newKey: {}", id, oldKey, newKey);
        return ImageResponseDto.fromEntity(existing);
    }

    /** 삭제 */
    @Transactional
    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));

        String key = extractKey(image.getPath());
        safeDeleteS3(key);
        imageRepository.delete(image);
        log.info("이미지 삭제 완료 - id: {}, key: {}", id, key);
    }

    // ===== 유틸 =====

    private void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ExpectedException(ErrorCode.INVALID_IMAGE_FILE);
    }

    private void validateImageCountLimit(Long userId, int willUploadCount) {
        long current = imageRepository.countByUserId(userId);
        int max = imageProperties.getMaxImagesPerUser();
        if (current + willUploadCount > max) throw new ExpectedException(ErrorCode.IMAGE_MAX_COUNT_EXCEEDED);
    }

    private String uuidName(String originalName) {
        String safe = Optional.ofNullable(originalName).orElse("file").replaceAll("[^a-zA-Z0-9._-]", "_");
        return UUID.randomUUID() + "-" + safe;
    }

    private String buildKey(Long userId, String serverFileName) {
        // 사용자별 폴더 원하면: return PREFIX + userId + "/" + serverFileName;
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
                    // 대용량이면 fromInputStream(...) 사용
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
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket).key(key).build());
        } catch (Exception e) {
            log.warn("S3 삭제 실패(무시) - key: {}", key, e);
        }
    }

    private String extractKey(String keyOrUrl) {
        if (keyOrUrl == null) return null;
        if (!keyOrUrl.startsWith("http")) return keyOrUrl;
        int idx = keyOrUrl.indexOf(".amazonaws.com/");
        if (idx == -1) return keyOrUrl;
        return keyOrUrl.substring(idx + ".amazonaws.com/".length());
    }

    private String s3Url(String key) {
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + key;
    }
}
