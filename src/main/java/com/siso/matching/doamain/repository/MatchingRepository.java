package com.siso.matching.doamain.repository;

import com.siso.matching.doamain.model.Matching;
import com.siso.matching.doamain.model.MatchingStatus;
import com.siso.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    Optional<Matching> findBySenderAndReceiver(User sender, User receiver);

    List<Matching> findAllByReceiverAndStatus(User receiver, MatchingStatus matchingStatus);
}
