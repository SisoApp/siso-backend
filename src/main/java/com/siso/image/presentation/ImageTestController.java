package com.siso.image.presentation;

import com.siso.image.dto.request.ImageRequestDto;
import com.siso.image.dto.response.ImageResponseDto;
import com.siso.image.application.ImageTestService;
import com.siso.image.infrastructure.ImageProperties;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 테스트용 이미지 API 컨트롤러
 * 
 * 개발 및 테스트 환경에서 사용할 수 있는 이미지 관련 API들을 제공합니다.
 * 인증 없이 userId를 path variable로 사용하여 테스트할 수 있습니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/test/images")
@RequiredArgsConstructor
public class ImageTestController {
    
    private final ImageTestService imageTestService;
    private final ImageProperties imageProperties;

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

    // ===================== 테스트용 이미지 CRUD API =====================

    /**
     * 테스트용 다중 이미지 업로드
     * 
     * @param files 업로드할 이미지 파일들
     * @param userId 사용자 ID
     * @return 업로드된 이미지 목록
     */
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

        // 4) 다중 파일 업로드 처리 (Presigned URL 자동 생성 포함)
        List<ImageResponseDto> responses = imageTestService.uploadMultipleImagesForTest(files, userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 테스트용 사용자별 이미지 목록 조회 (상세 정보 포함)
     * 
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 이미지 목록
     */
    @Operation(summary = "테스트용 사용자별 이미지 목록 조회 (상세 정보)")
    @GetMapping("/{userId}")
    public ResponseEntity<List<ImageResponseDto>> getImagesByUserIdForTest(@PathVariable Long userId) {
        List<ImageResponseDto> response = imageTestService.getImagesByUserIdForTest(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 테스트용 사용자별 이미지 목록 조회 (경량화)
     * 
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 경량화된 이미지 목록
     */
    @Operation(summary = "테스트용 사용자별 이미지 목록 조회 (경량화)")
    @GetMapping("/{userId}/lightweight")
    public ResponseEntity<List<ImageResponseDto>> getImagesByUserIdLightweightForTest(@PathVariable Long userId) {
        List<ImageResponseDto> response = imageTestService.getImagesByUserIdLightweightForTest(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 테스트용 이미지 단일 조회
     * 
     * @param imageId 이미지 ID
     * @return 이미지 정보
     */
    @Operation(summary = "테스트용 이미지 단일 조회")
    @GetMapping("/image/{imageId}")
    public ResponseEntity<ImageResponseDto> getImageForTest(@PathVariable Long imageId) {
        ImageResponseDto response = imageTestService.getImageForTest(imageId);
        return ResponseEntity.ok(response);
    }

    /**
     * 테스트용 이미지 수정
     * 
     * @param imageId 이미지 ID
     * @param userId 사용자 ID
     * @param file 업로드할 파일
     * @return 수정된 이미지 정보
     */
    @Operation(summary = "테스트용 이미지 수정")
    @PutMapping(value = "/{imageId}/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDto> updateImageForTest(@PathVariable Long imageId,
                                                               @PathVariable Long userId,
                                                               @RequestPart(value = "file", required = false) MultipartFile file) {
        ImageResponseDto response = imageTestService.updateImageForTest(imageId, file, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 테스트용 이미지 삭제
     * 
     * @param userId 사용자 ID
     * @param imageId 이미지 ID
     * @return 삭제 결과
     */
    @Operation(summary = "테스트용 이미지 삭제")
    @DeleteMapping("/{userId}/{imageId}")
    public ResponseEntity<Void> deleteImageForTest(@PathVariable Long userId, @PathVariable Long imageId) {
        imageTestService.deleteImageForTest(imageId);
        return ResponseEntity.noContent().build();
    }

    // ===================== 테스트용 Presigned URL 관리 API =====================

    /**
     * 간단한 테스트용 API (의존성 문제 확인용)
     */
    @Operation(summary = "간단한 테스트용 API")
    @GetMapping("/test-simple")
    public ResponseEntity<String> testSimple() {
        log.info("=== 간단한 테스트 API 호출됨 ===");
        return ResponseEntity.ok("테스트 성공! ImageTestController가 정상 작동합니다.");
    }

    /**
     * ImageTestService 의존성 테스트
     */
    @Operation(summary = "ImageTestService 의존성 테스트")
    @GetMapping("/test-service")
    public ResponseEntity<String> testService() {
        log.info("=== ImageTestService 의존성 테스트 시작 ===");
        try {
            if (imageTestService != null) {
                log.info("ImageTestService 정상 주입됨");
                String serviceResult = imageTestService.testSimple();
                log.info("ImageTestService.testSimple() 결과: {}", serviceResult);
                return ResponseEntity.ok("ImageTestService 정상 주입됨 - " + serviceResult);
            } else {
                log.error("ImageTestService가 null입니다");
                return ResponseEntity.status(500).body("ImageTestService가 null입니다");
            }
        } catch (Exception e) {
            log.error("ImageTestService 테스트 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("오류: " + e.getMessage());
        }
    }

    /**
     * Presigned URL 상태 확인 (디버깅용)
     * 
     * @param userId 사용자 ID
     * @return Presigned URL 상태 정보
     */
    @Operation(summary = "Presigned URL 상태 확인 (디버깅용)")
    @GetMapping("/check-presigned-url-status/{userId}")
    public ResponseEntity<String> checkPresignedUrlStatus(@PathVariable Long userId) {
        log.info("=== Presigned URL 상태 확인 API 호출됨 ===");
        log.info("userId: {}", userId);
        
        try {
            String statusInfo = imageTestService.checkPresignedUrlStatus(userId);
            return ResponseEntity.ok(statusInfo);
        } catch (Exception e) {
            log.error("Presigned URL 상태 확인 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("오류: " + e.getMessage());
        }
    }

    /**
     * 테스트용 특정 사용자의 만료된 Presigned URL 일괄 갱신
     * 
     * @param userId Presigned URL을 갱신할 사용자 ID
     * @return 갱신 결과 메시지
     */
    @Operation(summary = "테스트용 특정 사용자의 만료된 Presigned URL 일괄 갱신")
    @PostMapping("/refresh-expired-urls/{userId}")
    public ResponseEntity<String> refreshExpiredPresignedUrlsForTest(@PathVariable Long userId) {
        log.info("=== 테스트용 Presigned URL 갱신 API 호출됨 ===");
        log.info("userId: {}", userId);
        log.info("API 경로: /api/test/images/refresh-expired-urls/{}", userId);
        log.info("요청 시간: {}", java.time.LocalDateTime.now());
        
        try {
            // 해당 사용자의 만료된 Presigned URL들을 일괄 갱신
            log.info("ImageTestService 호출 시작...");
            int refreshedCount = imageTestService.refreshExpiredPresignedUrlsByUserIdForTest(userId);
            log.info("ImageTestService 호출 완료. 갱신된 개수: {}", refreshedCount);
            
            String result = String.format("사용자 %d의 만료된 Presigned URL %d개가 갱신되었습니다.", userId, refreshedCount);
            log.info("응답: {}", result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("=== 오류 발생 ===");
            log.error("오류 타입: {}", e.getClass().getSimpleName());
            log.error("오류 메시지: {}", e.getMessage());
            log.error("스택 트레이스:", e);
            throw e;
        }
    }
}
