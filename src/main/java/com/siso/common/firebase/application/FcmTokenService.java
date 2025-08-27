package com.siso.common.firebase.application;

import com.siso.common.firebase.domain.model.DeviceType;
import com.siso.common.firebase.domain.model.FcmToken;
import com.siso.common.firebase.domain.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * FCM 토큰 관리 서비스 클래스
 * 
 * 사용자의 FCM(Firebase Cloud Messaging) 토큰을 등록, 조회, 비활성화하는 기능을 제공합니다.
 * 각 사용자는 여러 디바이스를 가질 수 있으므로 여러 개의 FCM 토큰을 가질 수 있습니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenService {
    
    private final FcmTokenRepository fcmTokenRepository;
    
    /**
     * FCM 토큰을 저장하거나 업데이트합니다.
     * 
     * 동일한 사용자의 동일한 토큰이 이미 존재하는 경우 디바이스 타입을 업데이트하고,
     * 존재하지 않는 경우 새로운 토큰을 등록합니다.
     * 
     * @param userId 사용자 ID
     * @param token FCM 토큰 문자열
     * @param deviceType 디바이스 타입 (ANDROID 또는 IOS)
     */
    @Transactional
    public void saveOrUpdateToken(Long userId, String token, DeviceType deviceType) {
        // 기존 토큰이 있는지 확인
        fcmTokenRepository.findByUserIdAndTokenAndIsActiveTrue(userId, token)
                .ifPresentOrElse(
                        existingToken -> {
                            // 이미 존재하는 토큰이면 디바이스 타입 업데이트
                            existingToken.setDeviceType(deviceType);
                            log.info("Updated existing FCM token for user: {}", userId);
                        },
                        () -> {
                            // 새로운 토큰 저장
                            FcmToken fcmToken = FcmToken.builder()
                                    .userId(userId)
                                    .token(token)
                                    .deviceType(deviceType)
                                    .isActive(true)
                                    .build();
                            fcmTokenRepository.save(fcmToken);
                            log.info("Saved new FCM token for user: {}", userId);
                        }
                );
    }
    
    /**
     * 특정 사용자의 FCM 토큰을 비활성화합니다.
     * 
     * 사용자가 로그아웃하거나 앱을 삭제할 때 호출되어야 합니다.
     * 토큰을 완전히 삭제하지 않고 비활성화 상태로 변경하여 추적 가능성을 유지합니다.
     * 
     * @param userId 사용자 ID
     * @param token 비활성화할 FCM 토큰
     */
    @Transactional
    public void deactivateToken(Long userId, String token) {
        fcmTokenRepository.findByUserIdAndTokenAndIsActiveTrue(userId, token)
                .ifPresent(fcmToken -> {
                    fcmToken.setActive(false);
                    log.info("Deactivated FCM token for user: {}", userId);
                });
    }
    
    /**
     * 특정 사용자의 활성화된 모든 FCM 토큰을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 활성화된 FCM 토큰 목록
     */
    @Transactional(readOnly = true)
    public List<String> getActiveTokensByUserId(Long userId) {
        return fcmTokenRepository.findActiveTokensByUserId(userId);
    }
    
    /**
     * 여러 사용자의 활성화된 모든 FCM 토큰을 조회합니다.
     * 
     * 그룹 알림이나 공지사항 등을 전송할 때 사용됩니다.
     * 
     * @param userIds 사용자 ID 목록
     * @return 해당 사용자들의 활성화된 FCM 토큰 목록
     */
    @Transactional(readOnly = true)
    public List<String> getActiveTokensByUserIds(List<Long> userIds) {
        return fcmTokenRepository.findActiveTokensByUserIds(userIds);
    }
}
