package com.siso.common.firebase.application;


import com.siso.common.firebase.domain.model.FcmToken;
import com.siso.common.firebase.domain.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
     * 토큰 유효성 검증, 중복 방지, 비활성 토큰 재활성화를 모두 처리합니다.
     * 
     * @param userId 사용자 ID
     * @param token FCM 토큰 문자열
     */
    @Transactional
    public void saveOrUpdateToken(Long userId, String token) {
        // 1. 토큰 유효성 검증
        if (token == null || token.trim().isEmpty()) {
            log.warn("Invalid FCM token provided for user: {} - token is null or empty", userId);
            return;
        }
        
        String trimmedToken = token.trim();
        
        // 2. 해당 사용자에게 이미 동일한 토큰이 있는지 확인 (활성화 상태 무관)
        Optional<FcmToken> existingUserToken = fcmTokenRepository.findByUserIdAndToken(userId, trimmedToken);
        
        if (existingUserToken.isPresent()) {
            FcmToken userToken = existingUserToken.get();
            if (userToken.isActive()) {
                // 이미 활성화된 토큰이면 로그만 출력
                log.info("FCM token already exists and active for user: {}", userId);
            } else {
                // 비활성화된 토큰을 재활성화
                userToken.setActive(true);
                log.info("Reactivated existing FCM token for user: {}", userId);
            }
            return;
        }
        
        // 3. 다른 사용자가 이미 해당 토큰을 사용하고 있는지 확인
        List<FcmToken> otherUserTokens = fcmTokenRepository.findByToken(trimmedToken);
        if (!otherUserTokens.isEmpty()) {
            // 다른 사용자들의 토큰을 비활성화 (토큰은 하나의 디바이스에만 귀속되어야 함)
            otherUserTokens.forEach(otherToken -> {
                otherToken.setActive(false);
                log.info("Deactivated FCM token from user: {} as it's now used by user: {}", 
                        otherToken.getUserId(), userId);
            });
        }
        
        // 4. 새로운 토큰 저장
        FcmToken fcmToken = FcmToken.builder()
                .userId(userId)
                .token(trimmedToken)
                .isActive(true)
                .build();
        fcmTokenRepository.save(fcmToken);
        log.info("Saved new FCM token for user: {}", userId);
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
        if (token == null || token.trim().isEmpty()) {
            log.warn("Invalid FCM token provided for deactivation - user: {}", userId);
            return;
        }
        
        String trimmedToken = token.trim();
        
        fcmTokenRepository.findByUserIdAndToken(userId, trimmedToken)
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
