package com.siso.common.firebase.domain.repository;

import com.siso.common.firebase.domain.model.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FCM 토큰 데이터 액세스 레포지토리
 * 
 * FCM 토큰 엔티티에 대한 데이터베이스 조회 기능을 제공합니다.
 * Spring Data JPA를 사용하여 기본적인 CRUD 작업과 커스텀 쿼리를 지원합니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    
    /**
     * 특정 사용자의 활성화된 모든 FCM 토큰 엔티티를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 활성화된 FCM 토큰 엔티티 목록
     */
    List<FcmToken> findByUserIdAndIsActiveTrue(Long userId);
    
    /**
     * 특정 사용자의 특정 토큰이 활성화 상태인지 조회합니다.
     * 
     * @param userId 사용자 ID
     * @param token FCM 토큰 문자열
     * @return 조건에 맞는 FCM 토큰 엔티티 (Optional)
     */
    Optional<FcmToken> findByUserIdAndTokenAndIsActiveTrue(Long userId, String token);
    
    /**
     * 특정 사용자의 활성화된 FCM 토큰 문자열만 조회합니다.
     * 
     * FCM 메시지 전송 시 토큰 문자열만 필요한 경우 사용됩니다.
     * 성능 최적화를 위해 토큰 문자열만 SELECT합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 활성화된 FCM 토큰 문자열 목록
     */
    @Query("SELECT f.token FROM FcmToken f WHERE f.userId = :userId AND f.isActive = true")
    List<String> findActiveTokensByUserId(@Param("userId") Long userId);
    
    /**
     * 여러 사용자의 활성화된 FCM 토큰 문자열을 조회합니다.
     * 
     * 그룹 알림이나 공지사항 전송 시 사용됩니다.
     * 성능 최적화를 위해 토큰 문자열만 SELECT합니다.
     * 
     * @param userIds 사용자 ID 목록
     * @return 해당 사용자들의 활성화된 FCM 토큰 문자열 목록
     */
    @Query("SELECT f.token FROM FcmToken f WHERE f.userId IN :userIds AND f.isActive = true")
    List<String> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);
}
