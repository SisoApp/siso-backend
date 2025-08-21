package com.siso.notification.domain.repository;

import com.siso.notification.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 알림 데이터 액세스 레포지토리
 * 
 * 알림 엔티티에 대한 데이터베이스 조회 기능을 제공합니다.
 * Spring Data JPA를 사용하여 기본적인 CRUD 작업과 커스텀 쿼리를 지원합니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 특정 사용자가 받은 모든 알림을 최신순으로 조회합니다.
     * 
     * @param receiverId 수신자 ID
     * @return 해당 사용자의 알림 목록 (최신순)
     */
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    
    /**
     * 특정 사용자가 받은 읽지 않은 알림을 최신순으로 조회합니다.
     * 
     * @param receiverId 수신자 ID
     * @return 해당 사용자의 읽지 않은 알림 목록 (최신순)
     */
    List<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long receiverId);
    
    /**
     * 특정 사용자의 읽지 않은 알림 개수를 조회합니다.
     * 
     * @param receiverId 수신자 ID
     * @return 읽지 않은 알림 개수
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiverId = :receiverId AND n.isRead = false")
    long countUnreadByReceiverId(@Param("receiverId") Long receiverId);
    
    /**
     * 특정 사용자의 모든 알림을 읽음 처리합니다.
     * 
     * @param receiverId 수신자 ID
     */
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiverId = :receiverId AND n.isRead = false")
    void markAllAsReadByReceiverId(@Param("receiverId") Long receiverId);
}
