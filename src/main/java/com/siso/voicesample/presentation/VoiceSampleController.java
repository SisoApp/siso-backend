package com.siso.voicesample.presentation;

import com.siso.voicesample.application.dto.VoiceSampleRequestDto;
import com.siso.voicesample.application.dto.VoiceSampleResponseDto;
import com.siso.voicesample.application.service.VoiceSampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 음성 샘플 관련 API를 제공하는 컨트롤러
 * - 음성 파일 업로드/다운로드
 * - 음성 샘플 CRUD 작업
 * - 사용자별 음성 샘플 조회
 */
// @Slf4j
@RestController
@RequestMapping("/api/voice-samples") // 기본 API 경로: /api/voice-samples
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class VoiceSampleController {
    
    // 음성 샘플 비즈니스 로직을 처리하는 서비스
    private final VoiceSampleService voiceSampleService;
    
    /**
     * 음성 파일 업로드 API
     * 
     * @param file 업로드할 음성 파일 (MultipartFile)
     * @param request 음성 샘플 정보 (사용자 ID, 재생시간 등)
     * @return 업로드된 음성 샘플 정보
     * 
     * POST /api/voice-samples/upload
     * Content-Type: multipart/form-data
     * - file: 음성 파일
     * - data: { "userId": 1 } // duration은 파일에서 자동 추출 (최대 30초)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceSampleResponseDto> uploadVoiceSample(
            @RequestPart("file") MultipartFile file, // 업로드할 음성 파일
            @ModelAttribute @Valid VoiceSampleRequestDto request) { // 음성 샘플 메타데이터
        
        // log.info("음성 파일 업로드 요청 - 사용자: {}, 파일명: {}, 크기: {} bytes", 
        //         request.getUserId(), file.getOriginalFilename(), file.getSize());
        
        // 파일 업로드 및 데이터베이스 저장
        VoiceSampleResponseDto response = voiceSampleService.uploadVoiceSample(file, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 특정 사용자의 음성 샘플 목록 조회 API
     * 
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 음성 샘플 목록 (최신순 정렬)
     * 
     * GET /api/voice-samples/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VoiceSampleResponseDto>> getVoiceSamplesByUserId(@PathVariable Long userId) {
        // log.info("사용자 음성 샘플 목록 조회 요청 - 사용자: {}", userId);
        
        // 사용자별 음성 샘플 목록 조회 (생성일 기준 내림차순)
        List<VoiceSampleResponseDto> response = voiceSampleService.getVoiceSamplesByUserId(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 음성 샘플 단일 조회 API
     * 
     * @param id 조회할 음성 샘플 ID
     * @return 음성 샘플 상세 정보
     * 
     * GET /api/voice-samples/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<VoiceSampleResponseDto> getVoiceSample(@PathVariable Long id) {
        // log.info("음성 샘플 조회 요청 - ID: {}", id);
        
        // ID로 음성 샘플 단일 조회
        VoiceSampleResponseDto response = voiceSampleService.getVoiceSample(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 음성 샘플 수정 API (파일 교체)
     * 
     * @param id 수정할 음성 샘플 ID
     * @param file 새로운 음성 파일 (선택사항)
     * @param request 수정할 음성 샘플 정보
     * @return 수정된 음성 샘플 정보
     * 
     * PUT /api/voice-samples/{id}
     * Content-Type: multipart/form-data
     * - file: 새 음성 파일 (선택사항)
     * - data: { "userId": 1 } // duration은 새 파일에서 자동 추출 (최대 30초)
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceSampleResponseDto> updateVoiceSample(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file, // 새 음성 파일 (선택사항)
            @ModelAttribute @Valid VoiceSampleRequestDto request) { // 수정할 메타데이터
        
        // log.info("음성 샘플 수정 요청 - ID: {}, 파일교체: {}", id, file != null && !file.isEmpty());
        
        // 음성 샘플 수정 (파일 교체 포함)
        VoiceSampleResponseDto response = voiceSampleService.updateVoiceSample(id, file, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 음성 샘플 삭제 API
     * 
     * @param id 삭제할 음성 샘플 ID
     * @return 삭제 완료 응답 (204 No Content)
     * 
     * DELETE /api/voice-samples/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoiceSample(@PathVariable Long id) {
        // log.info("음성 샘플 삭제 요청 - ID: {}", id);
        
        // 음성 샘플 및 관련 파일 삭제
        voiceSampleService.deleteVoiceSample(id);
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }
    
    /**
     * 음성 파일 다운로드/스트리밍 API
     * 
     * @param filename 다운로드할 파일명
     * @return 음성 파일 리소스
     * 
     * GET /api/voice-samples/files/{filename}
     * 브라우저에서 직접 재생 가능하도록 inline으로 제공
     */
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            // 파일 경로 생성 및 정규화 (보안상 중요)
            Path filePath = Paths.get("uploads/voice-samples").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            // 파일 존재 여부 및 읽기 가능 여부 확인
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"") // inline은 브라우저에서 바로 재생
                        .contentType(MediaType.APPLICATION_OCTET_STREAM) // 바이너리 파일 타입
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
        } catch (MalformedURLException e) {
            // log.error("파일 다운로드 중 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }
    }
    
    // ===== 예외 처리 메서드들 =====
    
    /**
     * IllegalArgumentException 처리
     * 주로 잘못된 파라미터나 파일 검증 실패 시 발생
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        // log.warn("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage()); // 400 Bad Request
    }
    
    /**
     * RuntimeException 처리
     * 예상치 못한 서버 내부 오류 시 발생
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        // log.error("서버 오류: {}", e.getMessage());
        return ResponseEntity.internalServerError().body("서버 내부 오류가 발생했습니다."); // 500 Internal Server Error
    }
}
