package com.siso.voicesample.presentation;

import com.siso.voicesample.dto.response.VoiceSampleResponseDto;
import com.siso.voicesample.VoiceSampleTestService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 테스트용 음성 샘플 관련 API를 제공하는 컨트롤러
 *
 * 테스트 환경에서 사용할 수 있는 음성 관련 기능을 제공합니다.
 * @CurrentUser 없이 userId를 직접 받아서 처리합니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/voice-samples")
public class VoiceSampleTestController {

    private final VoiceSampleTestService voiceSampleTestService;

    // ===================== 테스트용 API =====================

    /**
     * 간단한 테스트용 메서드 (의존성 문제 확인용)
     */
    @GetMapping("/test")
    public ResponseEntity<String> testSimple() {
        log.info("=== VoiceSampleTestController.testSimple() 호출됨 ===");
        String result = voiceSampleTestService.testSimple();
        return ResponseEntity.ok(result);
    }

    /**
     * 테스트용 음성 파일 업로드 API (userId path variable)
     */
    @Operation(summary = "테스트용 음성 파일 업로드")
    @PostMapping(value = "/upload/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceSampleResponseDto> uploadVoiceSampleForTest(@RequestPart("file") MultipartFile file,
                                                                          @PathVariable Long userId) {
        log.info("=== 테스트용 음성 파일 업로드 API 호출됨 ===");
        log.info("userId: {}", userId);
        log.info("파일명: {}", file.getOriginalFilename());
        log.info("파일 크기: {} bytes", file.getSize());

        try {
            VoiceSampleResponseDto response = voiceSampleTestService.uploadVoiceSampleForTest(file, userId);
            log.info("테스트용 음성 파일 업로드 성공 - voiceId: {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("테스트용 음성 파일 업로드 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 테스트용 특정 사용자의 음성 샘플 목록 조회 API
     */
    @Operation(summary = "테스트용 음성 샘플 목록 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<List<VoiceSampleResponseDto>> getVoiceSamplesByUserIdForTest(@PathVariable Long userId) {
        log.info("=== 테스트용 음성 샘플 목록 조회 API 호출됨 ===");
        log.info("userId: {}", userId);

        try {
            List<VoiceSampleResponseDto> response = voiceSampleTestService.getVoiceSamplesByUserIdForTest(userId);
            log.info("테스트용 음성 샘플 목록 조회 성공 - 개수: {}", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("테스트용 음성 샘플 목록 조회 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 테스트용 음성 샘플 단일 조회 API
     */
    @Operation(summary = "테스트용 음성 샘플 단일 조회")
    @GetMapping("/{userId}/{voiceId}")
    public ResponseEntity<VoiceSampleResponseDto> getVoiceSampleForTest(@PathVariable Long userId,
                                                                       @PathVariable Long voiceId) {
        log.info("=== 테스트용 음성 샘플 단일 조회 API 호출됨 ===");
        log.info("userId: {}, voiceId: {}", userId, voiceId);

        try {
            VoiceSampleResponseDto response = voiceSampleTestService.getVoiceSampleForTest(voiceId);
            log.info("테스트용 음성 샘플 단일 조회 성공 - voiceId: {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("테스트용 음성 샘플 단일 조회 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 테스트용 음성 샘플 수정 API (파일 교체)
     */
    @Operation(summary = "테스트용 음성 샘플 수정")
    @PutMapping(value = "/{userId}/{voiceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceSampleResponseDto> updateVoiceSampleForTest(@PathVariable Long userId,
                                                                          @PathVariable Long voiceId,
                                                                          @RequestPart(value = "file", required = false) MultipartFile file) {
        log.info("=== 테스트용 음성 샘플 수정 API 호출됨 ===");
        log.info("userId: {}, voiceId: {}", userId, voiceId);
        log.info("파일 교체 여부: {}", file != null && !file.isEmpty());

        try {
            VoiceSampleResponseDto response = voiceSampleTestService.updateVoiceSampleForTest(voiceId, file, userId);
            log.info("테스트용 음성 샘플 수정 성공 - voiceId: {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("테스트용 음성 샘플 수정 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 테스트용 음성 샘플 삭제 API
     */
    @Operation(summary = "테스트용 음성 샘플 삭제")
    @DeleteMapping("/{userId}/{voiceId}")
    public ResponseEntity<Void> deleteVoiceSampleForTest(@PathVariable Long userId,
                                                         @PathVariable Long voiceId) {
        log.info("=== 테스트용 음성 샘플 삭제 API 호출됨 ===");
        log.info("userId: {}, voiceId: {}", userId, voiceId);

        try {
            voiceSampleTestService.deleteVoiceSampleForTest(voiceId);
            log.info("테스트용 음성 샘플 삭제 성공 - voiceId: {}", voiceId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("테스트용 음성 샘플 삭제 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ===================== Presigned URL 관리 API =====================

    /**
     * 테스트용 음성 샘플 Presigned URL 일괄 갱신 API
     */
    @Operation(summary = "테스트용 음성 샘플 Presigned URL 일괄 갱신")
    @PostMapping("/refresh-expired-urls/{userId}")
    public ResponseEntity<String> refreshExpiredPresignedUrlsForTest(@PathVariable Long userId) {
        log.info("=== 테스트용 음성 샘플 Presigned URL 일괄 갱신 API 호출됨 ===");
        log.info("userId: {}", userId);

        try {
            int refreshedCount = voiceSampleTestService.refreshExpiredPresignedUrlsByUserIdForTest(userId);
            String message = String.format("사용자 %d의 만료된 음성 샘플 Presigned URL %d개가 갱신되었습니다.", userId, refreshedCount);
            log.info("테스트용 Presigned URL 일괄 갱신 성공: {}", message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("테스트용 Presigned URL 일괄 갱신 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 테스트용 음성 샘플 Presigned URL 상태 확인 API
     */
    @Operation(summary = "테스트용 음성 샘플 Presigned URL 상태 확인")
    @GetMapping("/check-presigned-url-status/{userId}")
    public ResponseEntity<String> checkPresignedUrlStatusForTest(@PathVariable Long userId) {
        log.info("=== 테스트용 음성 샘플 Presigned URL 상태 확인 API 호출됨 ===");
        log.info("userId: {}", userId);

        try {
            String statusInfo = voiceSampleTestService.checkPresignedUrlStatus(userId);
            log.info("테스트용 Presigned URL 상태 확인 성공");
            return ResponseEntity.ok(statusInfo);
        } catch (Exception e) {
            log.error("테스트용 Presigned URL 상태 확인 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}
