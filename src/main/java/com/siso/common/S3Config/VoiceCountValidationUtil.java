package com.siso.common.S3Config;

import com.siso.voicesample.domain.repository.VoiceSampleRepository;
import com.siso.voicesample.infrastructure.properties.VoiceSampleProperties;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 음성 샘플 개수 제한 검증 유틸리티 클래스
 * 
 * 사용자별 음성 샘플 개수 제한을 검증하는 기능을 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceCountValidationUtil {

    private final VoiceSampleRepository voiceSampleRepository;
    private final VoiceSampleProperties voiceSampleProperties;

    /**
     * 음성 샘플 개수 제한 검증 (사용자당 최대 1개)
     * 
     * @param userId 사용자 ID
     * @throws ExpectedException 개수 제한을 초과한 경우
     */
    public void validateVoiceCountLimit(Long userId) {
        long current = voiceSampleRepository.countByUserId(userId);
        int max = voiceSampleProperties.getMaxVoiceSamplesPerUser();
        if (current >= max) {
            log.warn("음성 샘플 개수 제한 초과 - 사용자: {}, 현재: {}, 최대: {}", 
                    userId, current, max);
            throw new ExpectedException(ErrorCode.VOICE_SAMPLE_MAX_COUNT_EXCEEDED);
        }
    }
}
