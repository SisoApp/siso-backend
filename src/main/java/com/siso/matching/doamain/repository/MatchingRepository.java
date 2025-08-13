package com.siso.matching.doamain.repository;

import com.siso.matching.doamain.model.Matching;
import com.siso.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    @Query("SELECT m FROM Matching m WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId")
    Optional<Matching> findBySenderIdAndReceiverId(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Query("SELECT m FROM Matching m WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isLiked = :like")
    Optional<Matching> findBySenderIdAndReceiverIdAndLike(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId, @Param("like") boolean like);
}
