package com.siso.common.S3Config;

import com.siso.voicesample.domain.model.VoiceSample;
import com.siso.voicesample.domain.repository.VoiceSampleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VoiceSample Presigned URL 자동 관리 유틸리티 클래스
 * 
 * 음성 샘플의 Presigned URL을 자동으로 생성, 갱신, 관리하는 기능을 제공합니다.
 * VoiceSampleService와 VoiceSampleTestService에서 공통으로 사용됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoicePresignedUrlManagementUtil {

    private final VoiceSampleRepository voiceSampleRepository;
    private final VoiceS3PresignedUrlUtil voiceS3PresignedUrlUtil;
    private final VoiceS3KeyUtil voiceS3KeyUtil;

    /**
     * Presigned URL 자동 생성 및 저장
     * 
     * @param voiceSample 음성 샘플 엔티티
     * @param durationMinutes 유효 시간 (분)
     */
    @Transactional
    public void generateAndSavePresignedUrl(VoiceSample voiceSample, int durationMinutes) {
        try {
            String key = voiceS3KeyUtil.extractKey(voiceSample.getUrl());
            String presignedUrl = voiceS3PresignedUrlUtil.generatePresignedGetUrl(key);
            
            // 더 정확한 만료 시간 생성 (밀리초 단위까지 고려)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(durationMinutes);
            
            // VoiceSample 엔티티에 Presigned URL 정보 저장
            voiceSample.updatePresignedUrl(presignedUrl, expiresAt);
            voiceSampleRepository.save(voiceSample);
            
            log.info("음성 샘플 Presigned URL 자동 생성 완료 - voiceId: {}, 유효시간: {}분, 생성시간: {}, 만료시간: {}", 
                    voiceSample.getId(), durationMinutes, now, expiresAt);
                    
        } catch (Exception e) {
            log.error("음성 샘플 Presigned URL 자동 생성 실패 - voiceId: {}, message: {}", 
                    voiceSample.getId(), e.getMessage(), e);
            // Presigned URL 생성 실패해도 음성 샘플 조회는 가능하도록 예외를 던지지 않음
        }
    }

    /**
     * 만료된 Presigned URL들을 일괄 갱신
     */
    @Transactional
    public void refreshExpiredPresignedUrls() {
        List<VoiceSample> expiredVoiceSamples = voiceSampleRepository.findAll().stream()
                .filter(voiceSample -> !isPresignedUrlValid(voiceSample))
                .collect(Collectors.toList());
        
        for (VoiceSample voiceSample : expiredVoiceSamples) {
            generateAndSavePresignedUrl(voiceSample, 10); // 기본 10분 유효
        }
        
        log.info("만료된 음성 샘플 Presigned URL 일괄 갱신 완료 - 갱신된 음성 샘플 수: {}", expiredVoiceSamples.size());
    }

    /**
     * 특정 사용자의 만료된 Presigned URL들을 일괄 갱신
     * 
     * @param userId Presigned URL을 갱신할 사용자 ID
     * @return 갱신된 음성 샘플 수
     */
    @Transactional
    public int refreshExpiredPresignedUrlsByUserId(Long userId) {
        List<VoiceSample> userVoiceSamples = voiceSampleRepository.findByUserIdOrderByCreatedAtDesc(userId);
        int refreshedCount = 0;
        
        for (VoiceSample voiceSample : userVoiceSamples) {
            if (!isPresignedUrlValid(voiceSample)) {
                generateAndSavePresignedUrl(voiceSample, 10); // 기본 10분 유효
                refreshedCount++;
            }
        }
        
        log.info("사용자 {}의 만료된 음성 샘플 Presigned URL 일괄 갱신 완료 - 갱신된 음성 샘플 수: {}", userId, refreshedCount);
        return refreshedCount;
    }

    /**
     * 테스트용 특정 사용자의 만료된 Presigned URL들을 일괄 갱신
     * 
     * @param userId Presigned URL을 갱신할 사용자 ID
     * @return 갱신된 음성 샘플 수
     */
    @Transactional
    public int refreshExpiredPresignedUrlsByUserIdForTest(Long userId) {
        log.info("=== 음성 샘플 Presigned URL 관리 유틸리티 - 테스트용 갱신 시작 ===");
        log.info("입력 userId: {}", userId);
        
        try {
            log.info("사용자 음성 샘플 조회 시작...");
            List<VoiceSample> userVoiceSamples = voiceSampleRepository.findByUserIdOrderByCreatedAtDesc(userId);
            log.info("사용자 음성 샘플 조회 완료. 음성 샘플 개수: {}", userVoiceSamples.size());
            
            int refreshedCount = 0;
            
            for (VoiceSample voiceSample : userVoiceSamples) {
                log.info("음성 샘플 ID: {}, Presigned URL 유효성: {}", voiceSample.getId(), isPresignedUrlValid(voiceSample));
                if (!isPresignedUrlValid(voiceSample)) {
                    log.info("음성 샘플 ID {}의 Presigned URL 갱신 시작...", voiceSample.getId());
                    generateAndSavePresignedUrl(voiceSample, 10); // 기본 10분 유효
                    refreshedCount++;
                    log.info("음성 샘플 ID {}의 Presigned URL 갱신 완료", voiceSample.getId());
                }
            }
            
            log.info("전체 갱신 완료. 갱신된 음성 샘플 수: {}", refreshedCount);
            log.info("테스트용 사용자 {}의 만료된 음성 샘플 Presigned URL 일괄 갱신 완료 - 갱신된 음성 샘플 수: {}", userId, refreshedCount);
            return refreshedCount;
            
        } catch (Exception e) {
            log.error("=== 음성 샘플 Presigned URL 관리 유틸리티에서 오류 발생 ===");
            log.error("오류 타입: {}", e.getClass().getSimpleName());
            log.error("오류 메시지: {}", e.getMessage());
            log.error("스택 트레이스:", e);
            throw e;
        }
    }

    /**
     * Presigned URL 유효성 확인 (VoiceSample용)
     * 
     * @param voiceSample 음성 샘플 엔티티
     * @return 유효하면 true, 만료되었으면 false
     */
    private boolean isPresignedUrlValid(VoiceSample voiceSample) {
        if (voiceSample.getPresignedUrl() == null || voiceSample.getPresignedUrlExpiresAt() == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        // 보안을 위해 만료 1분 전부터는 유효하지 않은 것으로 처리
        LocalDateTime safetyMargin = voiceSample.getPresignedUrlExpiresAt().minusMinutes(1);
        return now.isBefore(safetyMargin);
    }
}
