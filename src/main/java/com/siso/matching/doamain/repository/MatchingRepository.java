package com.siso.matching.doamain.repository;

import com.siso.matching.doamain.model.Matching;
import com.siso.matching.doamain.model.MatchingStatus;
import com.siso.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    @Query("SELECT m FROM Matching m " +
            "WHERE (m.user1.id = :userA AND m.user2.id = :userB) " +
            "   OR (m.user1.id = :userB AND m.user2.id = :userA)")
    Optional<Matching> findByUsers(@Param("userA") Long userA, @Param("userB") Long userB);

    @Query("SELECT m FROM Matching m " +
            "WHERE (m.user1 = :user OR m.user2 = :user) " +
            "AND m.matchingStatus = :status")
    List<Matching> findAllByUserAndStatus(@Param("user") User user, @Param("status") MatchingStatus status);
}

