package com.siso.user.domain;

import com.siso.user.domain.model.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserInterest ui where ui.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}