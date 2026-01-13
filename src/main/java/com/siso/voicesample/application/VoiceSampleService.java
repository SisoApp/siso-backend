package com.siso.voicesample.application;

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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mpatric.mp3agic.Mp3File;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

/**
 * 음성 샘플 비즈니스 로직 처리 서비스
 *
 * 주요 기능:
 * - 음성 파일 업로드 및 메타데이터 추출 (duration 자동 계산)
 * - 음성 샘플 CRUD 작업 (생성, 조회, 수정, 삭제)
 * - 파일 저장소 관리 (로컬 파일 시스템)
 * - 재생 시간 제한 (20초) 처리
 *
 * 지원 파일 형식: MP3, WAV, M4A, AAC, OGG, WEBM, FLAC
 * 파일 크기 제한: 50MB
 *
 * @author SISO Team
 * @version 1.0
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoiceSampleService {

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

    // ===================== 공개 API 메서드들 =====================

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 음성 파일 업로드 및 저장
     *
     * 처리 과정:
     * 1. 파일 검증 (형식, 크기)
     * 2. 고유 파일명으로 저장
     * 3. Duration 자동 추출 (JAVE 라이브러리 → MP3AGIC → 파일크기 추정 순)
     * 4. 데이터베이스에 메타데이터 저장
     *
     * @param file 업로드할 음성 파일 (MultipartFile)
     * @param request 사용자 ID 등 추가 정보
     * @return 저장된 음성 샘플 정보 (실제 duration + 재생 제한 20초)
     * @throws IllegalArgumentException 파일 검증 실패 시
     * @throws RuntimeException 파일 저장 실패 시
     */
    @Transactional
    public VoiceSampleResponseDto uploadVoiceSample(MultipartFile file, VoiceSampleRequestDto request) {
        Long userId = request.getUserId();
        User user = findById(userId);

        // 사용자 존재 여부 확인
        userValidationUtil.validateUserExists(userId);

        // 음성 샘플 개수 제한 검증 (사용자당 최대 1개)
        voiceCountValidationUtil.validateVoiceCountLimit(userId);

        // S3 파일 처리: 검증 → 업로드 → duration 추출
        VoiceFileProcessResult result = processAudioFileToS3(file, userId);

        log.info("음성 파일 길이: {}초", result.getDuration());

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

        log.info("음성 샘플 업로드 완료 - ID: {}, 사용자: {}", savedVoiceSample.getId(), userId);

        return VoiceSampleResponseDto.fromEntity(savedVoiceSample);
    }

    /**
     * 특정 사용자의 음성 샘플 목록 조회
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 음성 샘플 목록 (생성일 기준 내림차순 정렬)
     */
    public List<VoiceSampleResponseDto> getVoiceSamplesByUserId(Long userId) {
        List<VoiceSample> voiceSamples = voiceSampleRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return voiceSamples.stream()
                .map(voiceSample -> {
                    // Presigned URL이 없거나 만료된 경우 자동으로 새로 생성
                    if (!voiceSample.isPresignedUrlValid()) {
                        log.info("만료된 Presigned URL 자동 갱신 - voiceId: {}", voiceSample.getId());
                        voicePresignedUrlManagementUtil.generateAndSavePresignedUrl(voiceSample, 10);
                    }
                    return VoiceSampleResponseDto.fromEntity(voiceSample);
                })
                .collect(Collectors.toList());
    }

    /**
     * 음성 샘플 단일 조회
     *
     * @param id 조회할 음성 샘플 ID
     * @return 음성 샘플 상세 정보
     * @throws RuntimeException 해당 ID의 음성 샘플이 존재하지 않는 경우
     */
    public VoiceSampleResponseDto getVoiceSample(Long id) {
        VoiceSample voiceSample = voiceSampleRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.VOICE_SAMPLE_NOT_FOUND));
        
        // Presigned URL이 없거나 만료된 경우 자동으로 새로 생성
        if (!voiceSample.isPresignedUrlValid()) {
            voicePresignedUrlManagementUtil.generateAndSavePresignedUrl(voiceSample, 10);
        }
        
        return VoiceSampleResponseDto.fromEntity(voiceSample);
    }

    /**
     * 음성 샘플 수정 (파일 교체)
     *
     * 처리 과정:
     * 1. 기존 음성 샘플 조회
     * 2. 새 파일이 있는 경우: 기존 파일 삭제 → 새 파일 처리 (검증, 저장, duration 추출)
     * 3. 메타데이터 업데이트 (userId, url, duration, fileSize)
     * 4. 데이터베이스 저장 (updatedAt 자동 갱신)
     *
     * @param id 수정할 음성 샘플 ID
     * @param file 새로운 음성 파일 (null 가능 - 메타데이터만 수정 시)
     * @param request 수정할 정보 (userId 등)
     * @return 수정된 음성 샘플 정보
     * @throws RuntimeException 해당 ID의 음성 샘플이 존재하지 않는 경우
     */
    @Transactional
    public VoiceSampleResponseDto updateVoiceSample(Long id, MultipartFile file, VoiceSampleRequestDto request) {
        Long userId = request.getUserId();
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

            log.info("파일 교체 완료 - 새 파일 길이: {}초", newDuration);
        }

        // userId는 이미 위에서 request.getUserId()로 가져왔고 유효성 검증도 완료

        // 기존 엔티티 업데이트 (BaseTime의 updatedAt 자동 갱신)
        existingVoiceSample.updateVoiceInfo(user, newFileUrl, newDuration, newFileSize);

        VoiceSample savedVoiceSample = voiceSampleRepository.save(existingVoiceSample);

        // Presigned URL 재생성
        voicePresignedUrlManagementUtil.generateAndSavePresignedUrl(savedVoiceSample, 10);

        log.info("음성 샘플 수정 완료 - ID: {}, 사용자: {}", savedVoiceSample.getId(), userId);

        return VoiceSampleResponseDto.fromEntity(savedVoiceSample);
    }

    /**
     * 음성 샘플 삭제
     *
     * 처리 과정:
     * 1. 기존 음성 샘플 조회
     * 2. 파일 시스템에서 음성 파일 삭제 (실패해도 계속 진행)
     * 3. 데이터베이스에서 레코드 삭제
     *
     * @param id 삭제할 음성 샘플 ID
     * @throws RuntimeException 해당 ID의 음성 샘플이 존재하지 않는 경우
     */
    @Transactional
    public void deleteVoiceSample(Long id) {
        VoiceSample voiceSample = voiceSampleRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.VOICE_SAMPLE_NOT_FOUND));

        User user = voiceSample.getUser();
        if (user != null) {
            user.setVoiceSample(null);     // User -> VoiceSample 참조 해제
        }
        voiceSample.setUser(null);


        // S3 파일 삭제
        String key = voiceS3KeyUtil.extractKey(voiceSample.getUrl());
        voiceS3DeleteUtil.safeDeleteS3(key);

        // 데이터베이스에서 레코드 삭제
        voiceSampleRepository.delete(voiceSample);
        voiceSampleRepository.flush();
        log.info("음성 샘플 삭제 완료 - ID: {}", id);
    }

    // ===================== 내부 헬퍼 메서드들 =====================

    /**
     * S3 음성 파일 처리 메서드
     *
     * 음성 파일의 전체 처리 과정을 하나의 메서드에서 담당:
     * 1. 파일 검증 (빈 파일, 파일명, 확장자, 크기)
     * 2. 고유 파일명 생성 및 S3 업로드
     * 3. Duration 자동 추출
     * 4. 결과 반환 (URL, duration, fileSize)
     *
     * @param file 처리할 음성 파일
     * @param userId 사용자 ID (폴더 구조에 사용)
     * @return 파일 처리 결과 (URL, duration, fileSize)
     * @throws IllegalArgumentException 파일 검증 실패 시
     * @throws RuntimeException 파일 업로드 실패 시
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

            // 지원 형식 검증 (프로퍼티 활용)
            String fileName = originalFileName.toLowerCase();
            String extension = originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";

            if (!voiceSampleProperties.isSupportedFormat(extension)) {
                throw new ExpectedException(ErrorCode.VOICE_SAMPLE_UNSUPPORTED_FORMAT);
            }

            // 파일 크기 검증 (프로퍼티 활용)
            if (file.getSize() > voiceSampleProperties.getMaxFileSize()) {
                throw new ExpectedException(ErrorCode.VOICE_SAMPLE_FILE_TOO_LARGE);
            }

            // === 2. S3 업로드 ===
            // 고유 파일명 생성
            String serverFileName = voiceS3KeyUtil.generateUuidName(originalFileName);
            String key = voiceS3KeyUtil.buildKey(userId, serverFileName);
            String contentType = Optional.ofNullable(file.getContentType()).orElse("audio/mpeg");

            // S3에 업로드
            voiceS3UploadUtil.putObject(key, file, contentType);

            // === 3. Duration 자동 추출 ===
            Integer duration = extractDurationFromFile(file);

            // === 3.5. Duration 제한 검증 (20초) ===
            if (duration > voiceSampleProperties.getMaxDuration()) {
                // S3 파일 삭제 후 예외 발생
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
     *
     * 추출 방법 (우선순위 순):
     * 1. JAVE 라이브러리 (FFmpeg 기반) - 모든 형식 지원, 가장 정확
     * 2. MP3AGIC 라이브러리 - MP3 전용, 정확한 메타데이터 추출
     * 3. 파일 크기 기반 추정 - 최후의 수단, 대략적인 값
     * 4. 기본값 6초 - 모든 방법 실패 시
     *
     * @param file 분석할 음성 파일
     * @return 추출된 duration (초 단위), 최소 1초
     */
    private Integer extractDurationFromFile(MultipartFile file) {
        java.io.File tempFile = null;
        try {
            // MultipartFile을 임시 파일로 저장
            tempFile = java.io.File.createTempFile("temp", ".audio");
            file.transferTo(tempFile);
            
            // === 1차 시도: JAVE 라이브러리 (FFmpeg 기반) ===
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

            // === 2차 시도: MP3AGIC 라이브러리 (MP3 전용) ===
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
                // M4A/AAC: 256kbps 가정
                estimatedDuration = (int) (fileSize / 32000);
            } else if (fileName.endsWith(".wav")) {
                // WAV: 44.1kHz, 16bit, stereo 무압축 가정
                estimatedDuration = (int) (fileSize / 176400);
            } else {
                // 기타 형식: 128kbps 가정
                estimatedDuration = (int) (fileSize / 16000);
            }

            log.info("파일 크기 기반 duration 추정: {}초 (크기: {} bytes)", estimatedDuration, fileSize);
            return Math.max(1, estimatedDuration);

        } catch (Exception e) {
            // === 4차 시도: 기본값 ===
            log.warn("모든 duration 추출 방법 실패: {}, 기본값 6초 사용", e.getMessage());
            return 6; // 일반적인 음성 샘플 길이
        } finally {
            // 임시 파일 정리
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // ===================== Presigned URL 관련 메서드들 =====================

    /**
     * 음성 샘플 Presigned GET URL 생성
     * 클라이언트가 임시로 음성 파일에 접근할 수 있는 URL을 생성합니다.
     * 
     * @param voiceId 음성 샘플 ID
     * @param userId 요청한 사용자 ID (소유권 검증용)
     * @return 10분간 유효한 presigned URL
     */
    public String getVoicePresignedUrl(Long voiceId, Long userId) {
        VoiceSample voiceSample = voiceSampleRepository.findById(voiceId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.VOICE_SAMPLE_NOT_FOUND));
        
        // 사용자 소유권 검증
        userValidationUtil.validateUserOwnership(voiceSample.getUser().getId(), userId);
        
        String key = voiceS3KeyUtil.extractKey(voiceSample.getUrl());
        String presignedUrl = voiceS3PresignedUrlUtil.generatePresignedGetUrl(key);
        
        log.info("음성 샘플 Presigned URL 생성 - voiceId: {}, userId: {}", voiceId, userId);
        return presignedUrl;
    }

    /**
     * 음성 샘플 단기 재생용 Presigned GET URL 생성 (3분 유효)
     * 빠른 미리보기나 짧은 재생에 사용
     * 
     * @param voiceId 음성 샘플 ID
     * @param userId 요청한 사용자 ID (소유권 검증용)
     * @return 3분간 유효한 presigned URL
     */
    public String getVoiceShortPlayPresignedUrl(Long voiceId, Long userId) {
        VoiceSample voiceSample = voiceSampleRepository.findById(voiceId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.VOICE_SAMPLE_NOT_FOUND));
        
        // 사용자 소유권 검증
        userValidationUtil.validateUserOwnership(voiceSample.getUser().getId(), userId);
        
        String key = voiceS3KeyUtil.extractKey(voiceSample.getUrl());
        String presignedUrl = voiceS3PresignedUrlUtil.generateShortPlayPresignedGetUrl(key);
        
        log.info("음성 샘플 단기 재생 Presigned URL 생성 - voiceId: {}, userId: {}", voiceId, userId);
        return presignedUrl;
    }

    // ===================== Presigned URL 관리 메서드들 =====================

    /**
     * 특정 사용자의 만료된 Presigned URL들을 일괄 갱신
     * 
     * @param userId Presigned URL을 갱신할 사용자 ID
     * @return 갱신된 음성 샘플 수
     */
    @Transactional
    public int refreshExpiredPresignedUrlsByUserId(Long userId) {
        return voicePresignedUrlManagementUtil.refreshExpiredPresignedUrlsByUserId(userId);
    }
}