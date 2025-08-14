package com.siso.voicesample.presentation;

import com.siso.voicesample.dto.VoiceSampleRequestDto;
import com.siso.voicesample.dto.VoiceSampleResponseDto;
import com.siso.voicesample.application.service.VoiceSampleService;
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
                // 파일 확장자에 따른 적절한 Content-Type 설정
                MediaType contentType = getMediaTypeForFilename(filename);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"") // inline은 브라우저에서 바로 재생
                        .contentType(contentType) // 음성 타입에 맞는 Content-Type
                        .body(resource);
            } else {
                throw new ExpectedException(ErrorCode.VOICE_SAMPLE_FILE_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new ExpectedException(ErrorCode.VOICE_SAMPLE_INVALID_PATH);
        }
    }

    // ===== 헬퍼 메서드들 =====

    /**
     * 파일명에서 확장자를 추출하여 적절한 MediaType 반환
     * 음성 파일 재생을 위한 올바른 Content-Type 설정
     *
     * @param filename 파일명
     * @return 해당 파일의 MediaType
     */
    private MediaType getMediaTypeForFilename(String filename) {
        String extension = filename.toLowerCase();

        if (extension.endsWith(".mp3")) {
            return MediaType.parseMediaType("audio/mpeg");
        } else if (extension.endsWith(".wav")) {
            return MediaType.parseMediaType("audio/wav");
        } else if (extension.endsWith(".m4a")) {
            return MediaType.parseMediaType("audio/mp4");
        } else if (extension.endsWith(".aac")) {
            return MediaType.parseMediaType("audio/aac");
        } else if (extension.endsWith(".ogg")) {
            return MediaType.parseMediaType("audio/ogg");
        } else if (extension.endsWith(".webm")) {
            return MediaType.parseMediaType("audio/webm");
        } else if (extension.endsWith(".flac")) {
            return MediaType.parseMediaType("audio/flac");
        } else {
            // 기본값: 일반 음성 타입
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}