package com.siso.common.util;

import com.siso.user.domain.repository.UserRepository;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 사용자 검증 관련 공통 유틸리티
 * 
 * 여러 서비스에서 공통으로 사용하는 사용자 검증 로직을 제공합니다.
 * 
 * 주요 기능:
 * - 사용자 존재 여부 확인
 * - 사용자 ID 유효성 검증
 * - 사용자 권한 관련 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidationUtil {
    
    private final UserRepository userRepository;
    
    /**
     * 사용자 존재 여부 확인
     * 
     * @param userId 확인할 사용자 ID
     * @throws ExpectedException 사용자가 존재하지 않거나 userId가 null인 경우
     */
    public void validateUserExists(Long userId) {
        if (userId == null) {
            log.warn("사용자 ID가 null입니다.");
            throw new ExpectedException(ErrorCode.USER_NOT_FOUND);
        }
        
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            log.warn("존재하지 않는 사용자 ID: {}", userId);
            throw new ExpectedException(ErrorCode.USER_NOT_FOUND);
        }
        
        log.debug("사용자 존재 확인 완료 - ID: {}", userId);
    }
    
    /**
     * 사용자 존재 여부를 boolean으로 반환
     * 
     * @param userId 확인할 사용자 ID
     * @return 사용자 존재 여부 (userId가 null이면 false)
     */
    public boolean isUserExists(Long userId) {
        if (userId == null) {
            return false;
        }
        return userRepository.existsById(userId);
    }
    
    /**
     * 여러 사용자 ID의 존재 여부를 한 번에 확인
     * 
     * @param userIds 확인할 사용자 ID 배열
     * @throws ExpectedException 존재하지 않는 사용자가 있는 경우
     */
    public void validateUsersExist(Long... userIds) {
        if (userIds == null || userIds.length == 0) {
            throw new ExpectedException(ErrorCode.USER_NOT_FOUND);
        }
        
        for (Long userId : userIds) {
            validateUserExists(userId);
        }
    }
    
    /**
     * 사용자 소유권 확인 (현재 사용자가 해당 리소스의 소유자인지 확인)
     * 
     * @param resourceOwnerId 리소스 소유자 ID
     * @param currentUserId 현재 사용자 ID
     * @throws ExpectedException 소유자가 아닌 경우
     */
    public void validateUserOwnership(Long resourceOwnerId, Long currentUserId) {
        validateUserExists(currentUserId);
        
        if (!resourceOwnerId.equals(currentUserId)) {
            log.warn("사용자 권한 없음 - 리소스 소유자: {}, 현재 사용자: {}", resourceOwnerId, currentUserId);
            throw new ExpectedException(ErrorCode.IMAGE_ACCESS_DENIED); // 또는 새로운 권한 관련 에러 코드
        }
        
        log.debug("사용자 소유권 확인 완료 - 사용자: {}", currentUserId);
    }
}
