package com.siso.user.domain.repository;

import com.siso.user.domain.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserProfileRepository extends JpaRepository<UserProfile,Long> {
    @Query("SELECT p FROM UserProfile p WHERE p.user.id = :userId")
    Optional<UserProfile> findByUserId(@Param("userId") Long userId);



    @Query(value = """
      SELECT p.*, 
             COALESCE(interest_match.common_interests, 0) as common_interests_count
      FROM user_profiles p
      JOIN users u ON u.id = p.user_id
      LEFT JOIN (
        SELECT 
          ui1.user_id,
          COUNT(*) as common_interests
        FROM user_interests ui1
        JOIN user_interests ui2 ON ui1.interest = ui2.interest
        WHERE ui2.user_id = :userId
          AND ui1.user_id != :userId
        GROUP BY ui1.user_id
      ) interest_match ON interest_match.user_id = u.id
      WHERE (:excludeId IS NULL OR u.id <> :excludeId)
        AND u.is_block = false 
        AND u.is_deleted = false
      ORDER BY
        -- 1순위: PreferenceSex 매칭
        CASE
          WHEN :preferenceSex IS NULL OR :preferenceSex = 'OTHER' THEN 0
          WHEN :preferenceSex = 'MALE'   AND p.sex = 'MALE'   THEN 0
          WHEN :preferenceSex = 'FEMALE' AND p.sex = 'FEMALE' THEN 0
          ELSE 1
        END,
        
        -- 2순위: 공통 관심사가 많은 순 (내림차순)
        COALESCE(interest_match.common_interests, 0) DESC,
        
        -- 3순위: 나이 차이 (4살 이내 우선)
        CASE
          WHEN :age IS NULL THEN 0
          WHEN ABS(p.age - :age) <= 4 THEN 0 
          ELSE 1
        END,
        
        -- 4순위: 기타 조건들
        CASE WHEN :location IS NOT NULL AND p.location = :location THEN 0 ELSE 1 END,
        CASE WHEN :religion IS NOT NULL AND p.religion = :religion THEN 0 ELSE 1 END,
        CASE WHEN :smoke IS NOT NULL AND p.smoke = :smoke THEN 0 ELSE 1 END,
        CASE WHEN :drinkingCapacity IS NOT NULL AND p.drinking_capacity = :drinkingCapacity THEN 0 ELSE 1 END,
        
        -- 5순위: 나이 차이 (절댓값)
        CASE WHEN :age IS NULL THEN 0 ELSE ABS(p.age - :age) END ASC,
        
        -- 6순위: 최근 활동 순
        CASE WHEN u.last_active_at IS NULL THEN 1 ELSE 0 END,
        u.last_active_at DESC,
        u.id DESC
      LIMIT :limit
    """, nativeQuery = true)
    List<UserProfile> findMatchingProfilesWithInterests(
            @Param("userId") Long userId,                        // 본인 ID (관심사 매칭용)
            @Param("excludeId") Long excludeId,                  // 제외할 ID (보통 본인)
            @Param("preferenceSex") String preferenceSex,        // 선호 성별
            @Param("religion") String religion,
            @Param("smoke") Boolean smoke,
            @Param("location") String location,
            @Param("drinkingCapacity") String drinkingCapacity,
            @Param("age") Integer age,
            @Param("limit") int limit
    );
}
