package com.siso.image.application;

import com.siso.image.domain.model.Image;
import com.siso.image.domain.model.FileProcessResult;
import com.siso.image.domain.repository.ImageRepository;
import com.siso.image.dto.request.ImageRequestDto;
import com.siso.image.dto.response.ImageResponseDto;
import com.siso.image.infrastructure.properties.ImageProperties;
import com.siso.image.infrastructure.handler.ImageFileHandler;
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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 이미지 비즈니스 로직 처리 서비스 (S3 업로드 적용)
 *
 * 변경 사항:
 * - 로컬 저장 대신 S3에 바로 업로드
 * - DB path에는 S3 URL을 저장 (기존 /api/images/view/{id} 덮어쓰기 제거)
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
    private final ImageFileHandler imageFileHandler; // 검증/이름 생성 등 일부 유틸은 그대로 사용 가능
    private final ImageProperties imageProperties;
    private final S3Client s3Client;

    private static final String PREFIX = "images/"; // S3 폴더 prefix

    // ===================== 공개 API 메서드들 =====================

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 이미지 파일 업로드 및 저장 (S3)
     */
    @Transactional
    public ImageResponseDto uploadImage(MultipartFile file, ImageRequestDto request) {
        Long userId = request.getUserId();
        User user = findById(userId);

        userValidationUtil.validateUserExists(userId);

        if (file == null || file.isEmpty()) {
            throw new ExpectedException(ErrorCode.IMAGE_NOT_FOUND);
        }

        long currentImageCount = imageRepository.countByUserId(userId);
        if (currentImageCount >= imageProperties.getMaxImagesPerUser()) {
            throw new ExpectedException(ErrorCode.IMAGE_MAX_COUNT_EXCEEDED);
        }

        // 파일명 생성 (안전한 이름 + UUID)
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String serverFileName = UUID.randomUUID() + "-" + safeName;

        // 필요 시 사용자별 폴더로 구분하려면 PREFIX + userId + "/" + serverFileName
        String key = PREFIX + serverFileName;

        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");

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
            log.error("S3 업로드 실패 - userId: {}, filename: {}", userId, serverFileName, e);
            throw new ExpectedException(ErrorCode.IMAGE_NOT_FOUND);
        }

        // 사용자 엔티티에 이미지 추가 (path에는 S3 URL 저장)
        user.addImage(
                s3Url(key),
                serverFileName,
                originalName
        );

        Image savedImage = imageRepository.findByServerImageName(serverFileName)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));

        // S3 URL 유지 (기존 /api/images/view/{id}로 덮어쓰지 않음)
        savedImage.setPath(s3Url(key));

        log.info("이미지 업로드 완료 - ID: {}, 사용자: {}, key: {}", savedImage.getId(), userId, key);
        return ImageResponseDto.fromEntity(savedImage);
    }

    /** 특정 사용자의 이미지 목록 조회 */
    public List<ImageResponseDto> getImagesByUserId(Long userId) {
        List<Image> images = imageRepository.findByUserIdOrderByCreatedAtAsc(userId);
        return images.stream()
                .map(ImageResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /** 이미지 단일 조회 */
    public ImageResponseDto getImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        return ImageResponseDto.fromEntity(image);
    }

    /** 이미지 수정 (파일 교체) — 현재는 기존 로직 유지 (로컬 삭제 호출 포함)
     *  필요 시 S3 교체/삭제 로직으로 전환 가능
     */
    @Transactional
    public ImageResponseDto updateImage(Long id, MultipartFile file, ImageRequestDto request) {
        Long userId = request.getUserId();

        Image existingImage = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));

        userValidationUtil.validateUserOwnership(existingImage.getUser().getId(), userId);

        if (file != null && !file.isEmpty()) {
            // 기존 로직 유지: 필요 시 S3 삭제 + 재업로드로 대체 가능
            imageFileHandler.deleteImageFile(existingImage.getServerImageName(), userId);

            FileProcessResult result = imageFileHandler.processImageFile(file, userId);
            existingImage.updateImage(
                    result.getFileUrl(),
                    result.getServerImageName(),
                    result.getOriginalName()
            );
            log.info("이미지 파일 교체 완료 - 기존: {}, 새파일: {}", existingImage.getServerImageName(), result.getServerImageName());
        }

        log.info("이미지 수정 완료 - ID: {}, 사용자: {}", existingImage.getId(), userId);
        return ImageResponseDto.fromEntity(existingImage);
    }

    /** 이미지 삭제 — 현재는 로컬 삭제 호출 유지 */
    @Transactional
    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));

        imageFileHandler.deleteImageFile(image.getServerImageName(), image.getUser().getId());
        imageRepository.delete(image);
        log.info("이미지 삭제 완료 - ID: {}", id);
    }

    // ===================== 내부 유틸 =====================

    private String s3Url(String key) {
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + key;
    }
}
