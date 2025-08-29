package com.siso.image.presentation;

import com.siso.common.web.CurrentUser;
import com.siso.image.dto.request.ImageRequestDto;
import com.siso.image.dto.response.ImageResponseDto;
import com.siso.image.application.ImageService;
import com.siso.image.infrastructure.properties.ImageProperties;
import com.siso.image.infrastructure.properties.MediaTypeProperties;
import com.siso.image.domain.model.Image;
import com.siso.image.domain.repository.ImageRepository;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final MediaTypeProperties mediaTypeProperties;
    private final ImageRepository imageRepository;
    
    // ===================== 이미지 CRUD API =====================
    
    /**
     * 통합 이미지 업로드 API
     * 
     * 단일 파일 또는 다중 파일 업로드를 모두 지원합니다.
     * - 단일 파일: @RequestPart("file") MultipartFile file
     * - 다중 파일: @RequestPart("files") List<MultipartFile> files
     * 
     * 한 번에 최대 5개까지 이미지를 업로드할 수 있습니다.
     * 사용자별 이미지 개수 제한(5개)을 초과하지 않도록 주의하세요.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(@RequestPart(value = "file", required = false) MultipartFile singleFile,
                                         @RequestPart(value = "files", required = false) List<MultipartFile> multipleFiles,
                                         @CurrentUser User user) {
        // 1) 인증 체크
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 2) 파일 입력 검증 (단일 또는 다중 중 하나만 허용)
        if (singleFile != null && multipleFiles != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "단일 파일과 다중 파일을 동시에 업로드할 수 없습니다.");
        }
        
        if (singleFile == null && (multipleFiles == null || multipleFiles.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        ImageRequestDto request = new ImageRequestDto(user.getId());

        // 3) 단일 파일 업로드 처리
        if (singleFile != null) {
            // 파일 기본 검증
            if (singleFile.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일이 비어 있습니다.");
            }

            // 형식/사이즈 검증
            validateFile(singleFile);

            // 실제 업로드
            ImageResponseDto response = imageService.uploadImage(singleFile, request);
            return ResponseEntity.ok(response);
        }

        // 4) 다중 파일 업로드 처리
        if (multipleFiles != null && !multipleFiles.isEmpty()) {
            // 파일 개수 제한 확인 (최대 5개)
            if (multipleFiles.size() > imageProperties.getMaxImagesPerUser()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "한 번에 최대 " + imageProperties.getMaxImagesPerUser() + "개까지 업로드할 수 있습니다.");
            }

            // 각 파일 검증
            for (MultipartFile file : multipleFiles) {
                if (file == null || file.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 파일이 포함되어 있습니다.");
                }
                validateFile(file);
            }

            // 실제 다중 업로드
            List<ImageResponseDto> responses = imageService.uploadMultipleImages(multipleFiles, request);
            return ResponseEntity.ok(responses);
        }

        // 이 부분은 도달하지 않아야 하지만 안전장치
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");
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
    
    // ===================== 이미지 뷰어 API =====================
    
    /**
     * 이미지 뷰어 API (다운로드 완전 방지)
     * 
     * 이미지 ID를 통해 안전하게 이미지를 볼 수 있습니다.
     * 사용자는 실제 파일명이나 경로를 알 필요 없이 imageId만으로 이미지를 볼 수 있습니다.
     * 다운로드는 절대 불가능하고 브라우저에서 보기만 가능합니다.
     * 
     * @param imageId 조회할 이미지 ID
     * @return 이미지 리소스 (뷰어용)
     * 
     * GET /api/images/view/{imageId}
     */
    @GetMapping("/view/{imageId}")
    public ResponseEntity<Resource> viewImage(@PathVariable(name = "imageId") Long imageId) {
        try {
            // 이미지 ID로 이미지 조회
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_FILE_NOT_FOUND));
            
            // 실제 파일명 가져오기
            String serverImageName = image.getServerImageName();
            if (serverImageName == null) {
                throw new ExpectedException(ErrorCode.IMAGE_FILE_NOT_FOUND);
            }
            
            // 사용자별 파일 경로 생성 및 정규화 (보안상 중요)
            Path filePath = Paths.get("uploads/images").resolve(image.getUser().toString()).resolve(serverImageName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            // 파일 존재 여부 및 읽기 가능 여부 확인
            if (resource.exists() && resource.isReadable()) {
                // Infrastructure Properties를 활용한 MediaType 결정
                MediaType contentType = mediaTypeProperties.determineContentType(serverImageName);
                
                return ResponseEntity.ok()
                        // 다운로드 완전 방지 헤더들
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // 파일명 없이 인라인 표시
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, private") // 캐시 완전 방지
                        .header(HttpHeaders.PRAGMA, "no-cache") // HTTP/1.0 캐시 방지
                        .header(HttpHeaders.EXPIRES, "0") // 만료 시간 0
                        .header("X-Content-Type-Options", "nosniff") // MIME 스니핑 방지
                        .header("X-Frame-Options", "DENY") // 프레임 내 표시 방지
                        .header("X-Download-Options", "noopen") // IE 다운로드 방지
                        .header("Content-Security-Policy", "default-src 'none'; img-src 'self'") // CSP 적용
                        .contentType(contentType)
                        .body(resource);
            } else {
                throw new ExpectedException(ErrorCode.IMAGE_FILE_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new ExpectedException(ErrorCode.IMAGE_INVALID_PATH);
        }
    }
}
