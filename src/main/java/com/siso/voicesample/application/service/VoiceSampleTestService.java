package com.siso.voicesample.application.service;

import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.voicesample.domain.model.VoiceSample;
import com.siso.voicesample.domain.model.VoiceFileProcessResult;
import com.siso.voicesample.domain.repository.VoiceSampleRepository;
import com.siso.voicesample.dto.request.VoiceSampleRequestDto;
import com.siso.voicesample.dto.response.VoiceSampleResponseDto;
import com.siso.voicesample.infrastructure.properties.VoiceSampleProperties;
import com.siso.common.util.UserValidationUtil;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.common.S3Config.VoiceS3UploadUtil;
import com.siso.common.S3Config.VoiceS3DeleteUtil;
import com.siso.common.S3Config.VoiceS3KeyUtil;
import com.siso.common.S3Config.VoiceS3PresignedUrlUtil;
import com.siso.common.S3Config.VoiceCountValidationUtil;
import com.siso.common.S3Config.VoicePresignedUrlManagementUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mpatric.mp3agic.Mp3File;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

/**
 * 테스트용 음성 샘플 비즈니스 로직 처리 서비스
 *
 * 테스트 환경에서 사용할 수 있는 음성 관련 기능을 제공합니다.
 * @CurrentUser 없이 userId를 직접 받아서 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoiceSampleTestService {

    // === 의존성 주입 ===
    /** 음성 샘플 데이터 접근 레이어 */
    private final VoiceSampleRepository voiceSampleRepository;

    /** 사용자 검증 유틸리티 */
    private final UserValidationUtil userValidationUtil;

    /** 음성 샘플 관련 설정 프로퍼티 */
    private final VoiceSampleProperties voiceSampleProperties;
    private final UserRepository userRepository;
    
    // S3 유틸리티 클래스들
    private final VoiceS3UploadUtil voiceS3UploadUtil;
    private final VoiceS3DeleteUtil voiceS3DeleteUtil;
    private final VoiceS3KeyUtil voiceS3KeyUtil;
    private final VoiceS3PresignedUrlUtil voiceS3PresignedUrlUtil;
    private final VoiceCountValidationUtil voiceCountValidationUtil;
    private final VoicePresignedUrlManagementUtil voicePresignedUrlManagementUtil;

    // ===================== 테스트용 API 메서드들 =====================

    /**
     * 간단한 테스트용 메서드 (의존성 문제 확인용)
     */
    public String testSimple() {
        log.info("=== VoiceSampleTestService.testSimple() 호출됨 ===");
        return "VoiceSampleTestService 정상 작동!";
    }

    public User findById(Long userId) {
        log.info("=== VoiceSampleTestService.findById() 호출됨 - userId: {} ===", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 테스트용 음성 파일 업로드 및 저장
     */
    @Transactional
    public VoiceSampleResponseDto uploadVoiceSampleForTest(MultipartFile file, Long userId) {
        User user = findById(userId);

        // 사용자 존재 여부 확인
        userValidationUtil.validateUserExists(userId);

        // 음성 샘플 개수 제한 검증 (사용자당 최대 1개)
        voiceCountValidationUtil.validateVoiceCountLimit(userId);

        // S3 파일 처리: 검증 → 업로드 → duration 추출
        VoiceFileProcessResult result = processAudioFileToS3(file, userId);

        log.info("테스트용 음성 파일 길이: {}초", result.getDuration());

        // 엔티티 생성 및 저장
        VoiceSample voiceSample = VoiceSample.builder()
                .user(user)
                .url(result.getFileUrl())
                .duration(result.getDuration())    // 실제 파일 길이
                .fileSize(result.getFileSize())
                .build();

        VoiceSample savedVoiceSample = voiceSampleRepository.save(voiceSample);

        // Presigned URL 자동 생성 및 저장
        voicePresignedUrlManagementUtil.generateAndSavePresignedUrl(savedVoiceSample, 10);

        log.info("테스트용 음성 샘플 업로드 완료 - ID: {}, 사용자: {}", savedVoiceSample.getId(), userId);

        return VoiceSampleResponseDto.fromEntity(savedVoiceSample);
    }

    /**
     * 테스트용 특정 사용자의 음성 샘플 목록 조회
     */
    public List<VoiceSampleResponseDto> getVoiceSamplesByUserIdForTest(Long userId) {
        List<VoiceSample> voiceSamples = voiceSampleRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return voiceSamples.stream()
                .map(voiceSample -> {
                    // Presigned URL이 없거나 만료된 경우 자동으로 새로 생성
                    if (!voiceSample.isPresignedUrlValid()) {
                        log.info("테스트용 만료된 Presigned URL 자동 갱신 - voiceId: {}", voiceSample.getId());
                        voicePresignedUrlManagementUtil.generateAndSavePresignedUrl(voiceSample, 10);
                    }
                    return VoiceSampleResponseDto.fromEntity(voiceSample);
                })
                .collect(Collectors.toList());
    }

    /**
     * 테스트용 음성 샘플 단일 조회
     */
    public VoiceSampleResponseDto getVoiceSampleForTest(Long id) {
        VoiceSample voiceSample = voiceSampleRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.VOICE_SAMPLE_NOT_FOUND));
        
        // Presigned URL이 없거나 만료된 경우 자동으로 새로 생성
        if (!voiceSample.isPresignedUrlValid()) {
            voicePresignedUrlManagementUtil.generateAndSavePresignedUrl(voiceSample, 10);
        }
        
        return VoiceSampleResponseDto.fromEntity(voiceSample);
    }

    /**
     * 테스트용 음성 샘플 수정 (파일 교체)
     */
    @Transactional
    public VoiceSampleResponseDto updateVoiceSampleForTest(Long id, MultipartFile file, Long userId) {
        User user = findById(userId);

        // 사용자 존재 여부 확인
        userValidationUtil.validateUserExists(userId);

        // 기존 음성 샘플 조회
        VoiceSample existingVoiceSample = voiceSampleRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.VOICE_SAMPLE_NOT_FOUND));

        // 음성 샘플 소유자 확인
        userValidationUtil.validateUserOwnership(existingVoiceSample.getUser().getId(), userId);

        // 새 파일이 제공된 경우에만 파일 교체
        String newFileUrl = existingVoiceSample.getUrl();
        Integer newFileSize = existingVoiceSample.getFileSize();
        Integer newDuration = existingVoiceSample.getDuration();

        if (file != null && !file.isEmpty()) {
            // 기존 S3 파일 삭제
            String oldKey = voiceS3KeyUtil.extractKey(existingVoiceSample.getUrl());
            voiceS3DeleteUtil.safeDeleteS3(oldKey);

            // S3 파일 처리: 검증 → 업로드 → duration 추출
            VoiceFileProcessResult result = processAudioFileToS3(file, userId);

            newFileUrl = result.getFileUrl();
            newFileSize = result.getFileSize();
            newDuration = result.getDuration();

            log.info("테스트용 파일 교체 완료 - 새 파일 길이: {}초", newDuration);
        }

        // 기존 엔티티 업데이트
        existingVoiceSample.setUser(user);
        existingVoiceSample.setUrl(newFileUrl);
        existingVoiceSample.setDuration(newDuration);
        existingVoiceSample.setFileSize(newFileSize);

        VoiceSample savedVoiceSample = voiceSampleRepository.save(existingVoiceSample);

        // Presigned URL 재생성
        voicePresignedUrlManagementUtil.generateAndSavePresignedUrl(savedVoiceSample, 10);

        log.info("테스트용 음성 샘플 수정 완료 - ID: {}, 사용자: {}", savedVoiceSample.getId(), userId);

        return VoiceSampleResponseDto.fromEntity(savedVoiceSample);
    }

    /**
     * 테스트용 음성 샘플 삭제
     */
    @Transactional
    public void deleteVoiceSampleForTest(Long id) {
        VoiceSample voiceSample = voiceSampleRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.VOICE_SAMPLE_NOT_FOUND));

        // S3 파일 삭제
        String key = voiceS3KeyUtil.extractKey(voiceSample.getUrl());
        voiceS3DeleteUtil.safeDeleteS3(key);

        // 데이터베이스에서 레코드 삭제
        voiceSampleRepository.delete(voiceSample);
        log.info("테스트용 음성 샘플 삭제 완료 - ID: {}", id);
    }

    /**
     * 테스트용 특정 사용자의 만료된 Presigned URL들을 일괄 갱신
     */
    @Transactional
    public int refreshExpiredPresignedUrlsByUserIdForTest(Long userId) {
        return voicePresignedUrlManagementUtil.refreshExpiredPresignedUrlsByUserIdForTest(userId);
    }

    /**
     * Presigned URL 상태 확인 (디버깅용)
     */
    public String checkPresignedUrlStatus(Long userId) {
        log.info("=== 음성 샘플 Presigned URL 상태 확인 시작 - userId: {} ===", userId);
        try {
            List<VoiceSample> userVoiceSamples = voiceSampleRepository.findByUserIdOrderByCreatedAtDesc(userId);
            StringBuilder statusInfo = new StringBuilder();
            statusInfo.append("=== 사용자 ").append(userId).append("의 음성 샘플 Presigned URL 상태 ===\n");
            statusInfo.append("현재 시간: ").append(LocalDateTime.now()).append("\n");
            statusInfo.append("음성 샘플 개수: ").append(userVoiceSamples.size()).append("\n\n");

            for (VoiceSample voiceSample : userVoiceSamples) {
                LocalDateTime now = LocalDateTime.now();
                boolean isValid = voiceSample.isPresignedUrlValid();

                statusInfo.append("음성 샘플 ID: ").append(voiceSample.getId()).append("\n");
                statusInfo.append("  - Presigned URL: ").append(voiceSample.getPresignedUrl() != null ? "있음" : "없음").append("\n");
                statusInfo.append("  - 만료 시간: ").append(voiceSample.getPresignedUrlExpiresAt()).append("\n");
                statusInfo.append("  - 현재 시간: ").append(now).append("\n");
                statusInfo.append("  - 유효 여부: ").append(isValid ? "유효" : "만료").append("\n");

                if (voiceSample.getPresignedUrlExpiresAt() != null) {
                    long minutesUntilExpiry = java.time.Duration.between(now, voiceSample.getPresignedUrlExpiresAt()).toMinutes();
                    statusInfo.append("  - 만료까지 남은 시간: ").append(minutesUntilExpiry).append("분\n");
                }
                statusInfo.append("\n");
            }
            log.info("음성 샘플 Presigned URL 상태 확인 완료");
            return statusInfo.toString();
        } catch (Exception e) {
            log.error("음성 샘플 Presigned URL 상태 확인 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ===================== 내부 헬퍼 메서드들 =====================

    /**
     * S3 음성 파일 처리 메서드
     */
    private VoiceFileProcessResult processAudioFileToS3(MultipartFile file, Long userId) {
        try {
            // === 1. 파일 검증 ===
            if (file.isEmpty()) {
                throw new ExpectedException(ErrorCode.VOICE_SAMPLE_FILE_EMPTY);
            }

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                throw new ExpectedException(ErrorCode.VOICE_SAMPLE_INVALID_FILENAME);
            }

            // 지원 형식 검증
            String fileName = originalFileName.toLowerCase();
            String extension = originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";

            if (!voiceSampleProperties.isSupportedFormat(extension)) {
                throw new ExpectedException(ErrorCode.VOICE_SAMPLE_UNSUPPORTED_FORMAT);
            }

            // 파일 크기 검증
            if (file.getSize() > voiceSampleProperties.getMaxFileSize()) {
                throw new ExpectedException(ErrorCode.VOICE_SAMPLE_FILE_TOO_LARGE);
            }

            // === 2. S3 업로드 ===
            String serverFileName = voiceS3KeyUtil.generateUuidName(originalFileName);
            String key = voiceS3KeyUtil.buildKey(userId, serverFileName);
            String contentType = Optional.ofNullable(file.getContentType()).orElse("audio/mpeg");

            voiceS3UploadUtil.putObject(key, file, contentType);

            // === 3. Duration 자동 추출 ===
            Integer duration = extractDurationFromFile(file);

            // === 3.5. Duration 제한 검증 (20초) ===
            if (duration > voiceSampleProperties.getMaxDuration()) {
                voiceS3DeleteUtil.safeDeleteS3(key);
                throw new ExpectedException(ErrorCode.VOICE_SAMPLE_FILE_TOO_LONG);
            }

            // === 4. 결과 반환 ===
            String fileUrl = voiceS3UploadUtil.generateS3Url(key);
            return VoiceFileProcessResult.of(fileUrl, duration, (int) file.getSize());

        } catch (Exception e) {
            log.error("파일 처리 중 오류 발생: {}", e.getMessage());
            throw new ExpectedException(ErrorCode.VOICE_SAMPLE_UPLOAD_FAILED);
        }
    }

    /**
     * 음성 파일에서 Duration 자동 추출
     */
    private Integer extractDurationFromFile(MultipartFile file) {
        java.io.File tempFile = null;
        try {
            tempFile = java.io.File.createTempFile("temp", ".audio");
            file.transferTo(tempFile);
            
            // === 1차 시도: JAVE 라이브러리 ===
            try {
                MultimediaObject multimediaObject = new MultimediaObject(tempFile);
                MultimediaInfo info = multimediaObject.getInfo();
                long durationInMillis = info.getDuration();
                int durationInSeconds = (int) (durationInMillis / 1000);

                log.info("JAVE로 duration 추출 성공: {}초 (파일: {})", durationInSeconds, file.getOriginalFilename());
                return Math.max(1, durationInSeconds);
            } catch (Exception e) {
                log.warn("JAVE 라이브러리 실패, 대체 방법 시도: {}", e.getMessage());
            }

            // === 2차 시도: MP3AGIC 라이브러리 ===
            String fileName = file.getOriginalFilename().toLowerCase();
            if (fileName.endsWith(".mp3")) {
                try {
                    Mp3File mp3file = new Mp3File(tempFile.getAbsolutePath());
                    int durationInSeconds = (int) mp3file.getLengthInSeconds();

                    log.info("MP3AGIC으로 duration 추출 성공: {}초", durationInSeconds);
                    return Math.max(1, durationInSeconds);
                } catch (Exception e) {
                    log.warn("MP3AGIC 라이브러리 실패: {}", e.getMessage());
                }
            }

            // === 3차 시도: 파일 크기 기반 추정 ===
            long fileSize = file.getSize();
            int estimatedDuration;

            if (fileName.endsWith(".m4a") || fileName.endsWith(".aac")) {
                estimatedDuration = (int) (fileSize / 32000);
            } else if (fileName.endsWith(".wav")) {
                estimatedDuration = (int) (fileSize / 176400);
            } else {
                estimatedDuration = (int) (fileSize / 16000);
            }

            log.info("파일 크기 기반 duration 추정: {}초 (크기: {} bytes)", estimatedDuration, fileSize);
            return Math.max(1, estimatedDuration);

        } catch (Exception e) {
            log.warn("모든 duration 추출 방법 실패: {}, 기본값 6초 사용", e.getMessage());
            return 6;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
