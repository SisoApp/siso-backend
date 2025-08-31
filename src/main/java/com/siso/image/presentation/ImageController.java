package com.siso.image.presentation;

import com.siso.common.web.CurrentUser;
import com.siso.image.dto.request.ImageRequestDto;
import com.siso.image.dto.response.ImageResponseDto;
import com.siso.image.application.ImageService;
import com.siso.image.infrastructure.properties.ImageProperties;
import com.siso.image.infrastructure.properties.MediaTypeProperties;
import com.siso.image.domain.repository.ImageRepository;
import com.siso.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 이미지 관련 API를 제공하는 통합 컨트롤러
 *
 * 이 컨트롤러는 infrastructure와 common 패키지의 기능들을 활용하여
 * 깔끔하고 유지보수하기 좋은 구조로 설계되었습니다.
 *
 * 주요 기능:
 * - 이미지 CRUD 작업 (업로드, 조회, 수정, 삭제)
 * - 이미지 뷰어 (다운로드 방지)
 * - 사용자 검증 (UserValidationUtil 활용)
 * - 파일 처리 (ImageFileProcessingService 활용)
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    // === 의존성 주입 ===
    private final ImageService imageService;
    private final ImageProperties imageProperties;

    // ===================== 이미지 CRUD API =====================

    /**
     * 이미지 업로드 API
     *
     * 다중 파일 업로드를 지원합니다.
     * - 다중 파일: @RequestPart("files") List<MultipartFile> files
     *
     * 한 번에 최대 5개까지 이미지를 업로드할 수 있습니다.
     * 사용자별 이미지 개수 제한(5개)을 초과하지 않도록 주의하세요.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(@RequestPart(value = "files") List<MultipartFile> files,
                                          @CurrentUser User user) {
        // 1) 인증 체크
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 2) 파일 입력 검증
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        // 3) 파일 개수 제한 확인 (최대 5개)
        if (files.size() > imageProperties.getMaxImagesPerUser()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "한 번에 최대 " + imageProperties.getMaxImagesPerUser() + "개까지 업로드할 수 있습니다.");
        }

        // 4) 각 파일 검증
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 파일이 포함되어 있습니다.");
            }
            validateFile(file);
        }

        // 5) 실제 업로드
        ImageRequestDto request = new ImageRequestDto(user.getId());
        List<ImageResponseDto> responses = imageService.uploadMultipleImages(files, request);
        return ResponseEntity.ok(responses);
    }

    /**
     * 파일 검증 헬퍼 메서드
     */
    private void validateFile(MultipartFile file) {
        // 형식 검사
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.') + 1)
                : "";
        if (!imageProperties.isSupportedFormat(ext)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "허용되지 않은 파일 형식입니다: " + originalName);
        }

        // 사이즈 검사
        if (file.getSize() > imageProperties.getMaxFileSize()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "파일은 최대 " + imageProperties.getMaxFileSizeInMB() + "MB까지 업로드할 수 있습니다: " + originalName);
        }
    }

    /**
     * 사용자별 이미지 목록 조회 API
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ImageResponseDto>> getImagesByUserId(@CurrentUser User user) {

        List<ImageResponseDto> response = imageService.getImagesByUserId(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 이미지 단일 조회 API
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<ImageResponseDto> getImage(@PathVariable(name = "imageId") Long imageId) {

        ImageResponseDto response = imageService.getImage(imageId);
        return ResponseEntity.ok(response);
    }

    /**
     * 이미지 수정 API
     */
    @PutMapping(value = "/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDto> updateImage(@PathVariable(name = "imageId") Long imageId,
                                                        @RequestPart(value = "file", required = false) MultipartFile file,
                                                        @CurrentUser User user) {

        ImageRequestDto request = new ImageRequestDto(user.getId());
        ImageResponseDto response = imageService.updateImage(imageId, file, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 이미지 삭제 API
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable(name = "imageId") Long imageId) {

        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    // 테스트용 이미지 업로드 API (userId path variable)
    @Operation(summary = "테스트용 다중 이미지 업로드")
    @PostMapping(value = "/upload/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImagesForTest(@RequestPart(value = "files") List<MultipartFile> files,
                                                 @PathVariable Long userId) {
        ImageRequestDto request = new ImageRequestDto(userId);

        // 1) 파일 입력 검증
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        // 2) 파일 개수 제한 확인 (최대 5개)
        if (files.size() > imageProperties.getMaxImagesPerUser()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "한 번에 최대 " + imageProperties.getMaxImagesPerUser() + "개까지 업로드할 수 있습니다.");
        }

        // 3) 각 파일 검증
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 파일이 포함되어 있습니다.");
            }
            validateFile(file);
        }

        // 4) 다중 파일 업로드 처리
        List<ImageResponseDto> responses = imageService.uploadMultipleImages(files, request);
        return ResponseEntity.ok(responses);
    }
}