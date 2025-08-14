package com.siso.image.domain.repository;

import com.siso.image.domain.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 이미지 데이터 접근 레이어
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    
    /**
     * 특정 사용자의 이미지 목록 조회 (생성일 기준 오름차순)
     */
    List<Image> findByUserIdOrderByCreatedAtAsc(Long userId);
    
    /**
     * 특정 사용자의 이미지 개수 조회
     */
    long countByUserId(Long userId);
}
