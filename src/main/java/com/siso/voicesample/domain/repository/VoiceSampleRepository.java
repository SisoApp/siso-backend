package com.siso.voicesample.domain.repository;

import com.siso.voicesample.domain.model.VoiceSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceSampleRepository extends JpaRepository<VoiceSample, Long> {
    
    /**
     * 특정 사용자의 음성 샘플 목록 조회
     */
    List<VoiceSample> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 특정 사용자의 음성 샘플 개수 조회
     */
    long countByUserId(Long userId);
    
    /**
     * 특정 기간 동안의 음성 샘플 조회
     */
    @Query("SELECT v FROM VoiceSample v WHERE v.userId = :userId AND v.createdAt >= :startDate")
    List<VoiceSample> findByUserIdAndCreatedAtAfter(@Param("userId") Long userId, 
                                                   @Param("startDate") java.time.LocalDateTime startDate);
}