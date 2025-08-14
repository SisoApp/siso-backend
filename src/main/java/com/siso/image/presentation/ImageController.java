package com.siso.image.presentation;

import com.siso.image.dto.ImageRequestDto;
import com.siso.image.dto.ImageResponseDto;
import com.siso.image.application.service.ImageService;
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
 * 이미지 관련 API를 제공하는 컨트롤러
 * - 이미지 파일 업로드/다운로드 (사용자당 최대 5개)
 * - 이미지 CRUD 작업
 * - 사용자별 이미지 조회
 */
@RestController
@RequestMapping("/api/images") // 기본 API 경로: /api/images
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class ImageController {
    
    // 이미지 비즈니스 로직을 처리하는 서비스
    private final ImageService imageService;
    
    /**
     * 이미지 파일 업로드 API
     * 
     * @param file 업로드할 이미지 파일 (MultipartFile)
     * @param request 이미지 정보 (사용자 ID 등)
     * @return 업로드된 이미지 정보
     * 
     * POST /api/images/upload
     * Content-Type: multipart/form-data
     * - file: 이미지 파일
     * - data: { "userId": 1 }
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDto> uploadImage(
            @RequestPart("file") MultipartFile file, // 업로드할 이미지 파일
            @ModelAttribute @Valid ImageRequestDto request) { // 이미지 메타데이터
        
        // 파일 업로드 및 데이터베이스 저장
        ImageResponseDto response = imageService.uploadImage(file, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 특정 사용자의 이미지 목록 조회 API
     * 
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 이미지 목록 (생성일 기준 오름차순 정렬)
     * 
     * GET /api/images/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ImageResponseDto>> getImagesByUserId(@PathVariable Long userId) {
        
        // 사용자별 이미지 목록 조회 (생성일 기준 오름차순)
        List<ImageResponseDto> response = imageService.getImagesByUserId(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이미지 단일 조회 API
     * 
     * @param id 조회할 이미지 ID
     * @return 이미지 상세 정보
     * 
     * GET /api/images/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ImageResponseDto> getImage(@PathVariable Long id) {
        
        // ID로 이미지 단일 조회
        ImageResponseDto response = imageService.getImage(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이미지 수정 API (파일 교체)
     * 
     * 
     * @param id 수정할 이미지 ID
     * @param userId 수정할 사용자 ID (프로필 사진용)
     * @param file 새로운 이미지 파일 (선택사항)
     * @param request 수정할 이미지 정보
     * @return 수정된 이미지 정보
     * 
     * PUT /api/images/{id}
     * Content-Type: multipart/form-data
     * - file: 새 이미지 파일 (선택사항)
     * - data: { "userId": 1 }
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDto> updateImage(
            @PathVariable Long id,
            // @RequestParam(value = "userId", required = false) Long userId,
            @RequestPart(value = "file", required = false) MultipartFile file, // 새 이미지 파일 (선택사항)
            @ModelAttribute @Valid ImageRequestDto request) { // 수정할 메타데이터
        
        // 이미지 수정 (파일 교체 포함)
        ImageResponseDto response = imageService.updateImage(id, file, request);
        // ImageResponseDto response = imageService.updateImage(id, userId, file, request);// 프로필 사진 수정용
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이미지 삭제 API
     * 
     * @param id 삭제할 이미지 ID
     * @return 삭제 완료 응답 (204 No Content)
     * 
     * DELETE /api/images/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        
        // 이미지 및 관련 파일 삭제
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }
    
    /**
     * 이미지 파일 다운로드/스트리밍 API
     * 
     * @param filename 다운로드할 파일명
     * @return 이미지 파일 리소스
     * 
     * GET /api/images/files/{filename}
     * 브라우저에서 직접 표시 가능하도록 inline으로 제공
     */
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            // 파일 경로 생성 및 정규화 (보안상 중요)
            Path filePath = Paths.get("uploads/images").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            // 파일 존재 여부 및 읽기 가능 여부 확인
            if (resource.exists() && resource.isReadable()) {
                // 파일 확장자에 따른 적절한 Content-Type 설정
                MediaType contentType = getMediaTypeForFilename(filename);
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"") // inline은 브라우저에서 바로 표시
                        .contentType(contentType) // 이미지 타입에 맞는 Content-Type
                        .body(resource);
            } else {
                throw new ExpectedException(ErrorCode.IMAGE_FILE_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new ExpectedException(ErrorCode.IMAGE_INVALID_PATH);
        }
    }
    
    // ===== 헬퍼 메서드들 =====
    
    /**
     * 파일명에서 확장자를 추출하여 적절한 MediaType 반환
     * 이미지 다운로드가 아닌 뷰어
     * @param filename 파일명
     * @return 해당 파일의 MediaType
     */
    private MediaType getMediaTypeForFilename(String filename) {
        String extension = filename.toLowerCase();
        
        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (extension.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (extension.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (extension.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        } else {
            // 기본값: 일반 이미지 타입
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
    
}
