package com.siso.voicesample.presentation;

import com.siso.common.web.CurrentUser;
import com.siso.user.domain.model.User;
import com.siso.voicesample.dto.request.VoiceSampleRequestDto;
import com.siso.voicesample.dto.response.VoiceSampleResponseDto;
import com.siso.voicesample.application.service.VoiceSampleService;
import com.siso.voicesample.infrastructure.properties.VoiceMediaTypeProperties;
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
 *
 * 주요 기능:
 * - 음성 파일 업로드 (사용자당 최대 1개 제한)
 * - 음성 샘플 CRUD 작업 (생성, 조회, 수정, 삭제)
 * - 사용자별 음성 샘플 조회
 * - Duration 자동 추출 (최대 20초 제한)
 * - 음성 재생 뷰어 (voiceId 기반)
 *
 * 비즈니스 규칙:
 * - 사용자당 음성 샘플 1개만 허용 (개수 제한 강화)
 * - 기존 음성이 있으면 업로드 차단
 * - 수정 시에는 기존 파일 교체만 가능
 *
 * 보안 정책:
 * - 파일 다운로드 완전 방지 (이미지보다 더 강화)
 * - voiceId 기반 안전한 재생만 허용
 * - 실제 파일명/경로 노출 차단
 * - Range 요청 비활성화로 다운로드 방지 강화
 */
// @Slf4j
@RestController
@RequestMapping("/api/voice-samples") // 기본 API 경로: /api/voice-samples
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class VoiceSampleController {

    // 음성 샘플 비즈니스 로직을 처리하는 서비스
    private final VoiceSampleService voiceSampleService;
    // 음성 미디어 타입 처리 프로퍼티
    private final VoiceMediaTypeProperties voiceMediaTypeProperties;

    /**
     * 음성 파일 업로드 API
     *
     * @param file 업로드할 음성 파일 (MultipartFile)
     * @return 업로드된 음성 샘플 정보
     *
     * POST /api/voice-samples/upload
     * Content-Type: multipart/form-data
     * - file: 음성 파일
     * - userId: 1 // 사용자 ID (필수), duration은 파일에서 자동 추출 (최대 20초)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceSampleResponseDto> uploadVoiceSample(@RequestPart("file") MultipartFile file, // 업로드할 음성 파일
                                                                    @CurrentUser User user) { // 사용자 ID

        // log.info("음성 파일 업로드 요청 - 사용자: {}, 파일명: {}, 크기: {} bytes",
        //         userId, file.getOriginalFilename(), file.getSize());

        // VoiceSampleRequestDto 생성
        VoiceSampleRequestDto request = new VoiceSampleRequestDto();
        request.setUserId(user.getId());

        // 파일 업로드 및 데이터베이스 저장
        VoiceSampleResponseDto response = voiceSampleService.uploadVoiceSample(file, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 사용자의 음성 샘플 목록 조회 API
     *
     * @return 해당 사용자의 음성 샘플 목록 (최신순 정렬)
     *
     * GET /api/voice-samples/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VoiceSampleResponseDto>> getVoiceSamplesByUserId(@CurrentUser User user) {
        // log.info("사용자 음성 샘플 목록 조회 요청 - 사용자: {}", userId);

        // 사용자별 음성 샘플 목록 조회 (생성일 기준 내림차순)
        List<VoiceSampleResponseDto> response = voiceSampleService.getVoiceSamplesByUserId(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 음성 샘플 수정 API (파일 교체)
     * 
     * @param voiceId 수정할 음성 샘플 ID
     * @param file 새로운 음성 파일 (선택사항)
     * @return 수정된 음성 샘플 정보
     * 
     * PUT /api/voice-samples/{voiceId}
     * Content-Type: multipart/form-data
     * - file: 새 음성 파일 (선택사항)
     * - userId: 1 // 사용자 ID (필수), duration은 새 파일에서 자동 추출 (최대 20초)
     */
    @PutMapping(value = "/{voiceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceSampleResponseDto> updateVoiceSample(@PathVariable(name = "voiceId") Long voiceId,
                                                                    @RequestPart(value = "file", required = false) MultipartFile file, // 새 음성 파일 (선택사항)
                                                                    @CurrentUser User user) { // 사용자 ID
        
        // log.info("음성 샘플 수정 요청 - ID: {}, 파일교체: {}", voiceId, file != null && !file.isEmpty());
        
        // VoiceSampleRequestDto 생성
        VoiceSampleRequestDto request = new VoiceSampleRequestDto();
        request.setUserId(user.getId());

        // 음성 샘플 수정 (파일 교체 포함)
        VoiceSampleResponseDto response = voiceSampleService.updateVoiceSample(voiceId, file, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{voiceId}")
    public ResponseEntity<Void> deleteVoiceSample(@PathVariable(name = "voiceId") Long voiceId) {
        // log.info("음성 샘플 삭제 요청 - ID: {}", voiceId);
        
        // 음성 샘플 및 관련 파일 삭제
        voiceSampleService.deleteVoiceSample(voiceId);
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }

    // ===================== 음성 뷰어 API =====================

    /**
     * 음성 뷰어 API (다운로드 완전 방지)
     *
     * 음성 ID를 통해 안전하게 음성을 재생할 수 있습니다.
     * 사용자는 실제 파일명이나 경로를 알 필요 없이 voiceId만으로 음성을 들을 수 있습니다.
     * 다운로드는 절대 불가능하고 브라우저에서 재생만 가능합니다.
     *
     * @param voiceId 재생할 음성 샘플 ID
     * @return 음성 리소스 (재생용)
     *
     * GET /api/voice-samples/play/{voiceId}
     */
    @GetMapping("/play/{voiceId}")
    public ResponseEntity<Resource> playVoice(@PathVariable(name = "voiceId") Long voiceId) {
        try {
            // 음성 샘플 ID로 음성 정보 조회
            VoiceSampleResponseDto voiceInfo = voiceSampleService.getVoiceSample(voiceId);

            // 실제 파일명 추출 (URL에서 파일명 부분만)
            String filename = voiceMediaTypeProperties.extractFilenameFromUrl(voiceInfo.getUrl());
            if (filename == null) {
                throw new ExpectedException(ErrorCode.VOICE_SAMPLE_INVALID_PATH);
            }

            // 사용자별 파일 경로 생성 및 정규화 (보안상 중요)
            Path filePath = Paths.get("uploads/voice-samples").resolve(voiceInfo.getUserId().toString()).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // 파일 존재 여부 및 읽기 가능 여부 확인
            if (resource.exists() && resource.isReadable()) {
                // Infrastructure Properties를 활용한 MediaType 결정
                MediaType contentType = voiceMediaTypeProperties.determineContentType(filename);

                return ResponseEntity.ok()
                        // 다운로드 완전 방지 헤더들 (이미지보다 더 강화)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // 파일명 없이 인라인 재생
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, private") // 캐시 완전 방지
                        .header(HttpHeaders.PRAGMA, "no-cache") // HTTP/1.0 캐시 방지
                        .header(HttpHeaders.EXPIRES, "0") // 만료 시간 0
                        .header("X-Content-Type-Options", "nosniff") // MIME 스니핑 방지
                        .header("X-Frame-Options", "DENY") // 프레임 내 표시 방지
                        .header("X-Download-Options", "noopen") // IE 다운로드 방지
                        .header("Content-Security-Policy", "default-src 'none'; media-src 'self'") // CSP 적용 (audio 전용)
                        .header("Accept-Ranges", "none") // Range 요청 비활성화 (다운로드 방지 강화)
                        .contentType(contentType)
                        .body(resource);
            } else {
                throw new ExpectedException(ErrorCode.VOICE_SAMPLE_FILE_NOT_FOUND);
            }
        } catch (ExpectedException e) {
            // 비즈니스 예외는 그대로 전파
            throw e;
        } catch (MalformedURLException e) {
            throw new ExpectedException(ErrorCode.VOICE_SAMPLE_INVALID_PATH);
        }
    }
}