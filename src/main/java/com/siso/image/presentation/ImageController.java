package com.siso.image.presentation;

import com.siso.image.dto.ImageRequestDto;
import com.siso.image.dto.ImageResponseDto;
import com.siso.image.application.service.ImageService;
import com.siso.image.infrastructure.properties.ImageFileHandler;
import com.siso.image.infrastructure.properties.MediaTypeProperties;
import com.siso.image.domain.model.Image;
import com.siso.image.domain.repository.ImageRepository;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
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
    private final ImageFileHandler imageFileHandler;
    private final MediaTypeProperties mediaTypeProperties;
    private final ImageRepository imageRepository;
    
    // ===================== 이미지 CRUD API =====================
    
    /**
     * 이미지 업로드 API
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDto> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam("userId") @Valid Long userId) {
        
        ImageRequestDto request = new ImageRequestDto(userId);
        ImageResponseDto response = imageService.uploadImage(file, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 사용자별 이미지 목록 조회 API
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ImageResponseDto>> getImagesByUserId(@PathVariable Long userId) {
        
        List<ImageResponseDto> response = imageService.getImagesByUserId(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이미지 단일 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<ImageResponseDto> getImage(@PathVariable Long id) {
        
        ImageResponseDto response = imageService.getImage(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 이미지 수정 API
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDto> updateImage(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam("userId") @Valid Long userId) {
        
        ImageRequestDto request = new ImageRequestDto(userId);
        ImageResponseDto response = imageService.updateImage(id, file, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이미지 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
    
    // ===================== 이미지 뷰어 API =====================
    
    /**
     * 이미지 뷰어 API (다운로드 완전 방지)
     * 
     * MediaTypeProperties를 활용하여 안전한 이미지 뷰어 제공
     * 다운로드는 절대 불가능하고 브라우저에서 보기만 가능
     */
    @GetMapping("/view/{serverfilename}")
    public ResponseEntity<Resource> viewImage(@PathVariable String serverfilename) {
        try {
            // 데이터베이스에서 serverImageName으로 이미지 조회하여 userId 획득
            Image image = imageRepository.findByServerImageName(serverfilename)
                    .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_FILE_NOT_FOUND));
            
            // 사용자별 파일 경로 생성 및 정규화 (보안상 중요)
            Path filePath = Paths.get("uploads/images").resolve(image.getUserId().toString()).resolve(serverfilename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            // 파일 존재 여부 및 읽기 가능 여부 확인
            if (resource.exists() && resource.isReadable()) {
                // Infrastructure Properties를 활용한 MediaType 결정
                MediaType contentType = mediaTypeProperties.determineContentType(serverfilename);
                
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
