package com.siso.image.domain.repository;

import com.siso.image.domain.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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


    /**
     * 서버에 저장된 이미지 파일 이름을 기준으로 Image 엔티티를 조회합니다.
     * * @param serverImageName 서버에 저장된 파일 이름
     * @return 주어진 이름과 일치하는 Image 엔티티를 담은 Optional 객체
     */
    Optional<Image> findByServerImageName(String serverImageName);
}
