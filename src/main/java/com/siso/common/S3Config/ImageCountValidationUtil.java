package com.siso.common.S3Config;

import com.siso.image.domain.repository.ImageRepository;
import com.siso.image.infrastructure.properties.ImageProperties;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 이미지 개수 제한 검증 유틸리티 클래스
 * 
 * 사용자별 이미지 개수 제한을 검증하는 기능을 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCountValidationUtil {

    private final ImageRepository imageRepository;
    private final ImageProperties imageProperties;

    /**
     * 이미지 개수 제한 검증
     * 
     * @param userId 사용자 ID
     * @param willUploadCount 업로드 예정인 이미지 개수
     * @throws ExpectedException 개수 제한을 초과한 경우
     */
    public void validateImageCountLimit(Long userId, int willUploadCount) {
        long current = imageRepository.countByUserId(userId);
        int max = imageProperties.getMaxImagesPerUser();
        if (current + willUploadCount > max) {
            log.warn("이미지 개수 제한 초과 - 사용자: {}, 현재: {}, 업로드 예정: {}, 최대: {}", 
                    userId, current, willUploadCount, max);
            throw new ExpectedException(ErrorCode.IMAGE_MAX_COUNT_EXCEEDED);
        }
    }
}