package com.siso.call.domain.repository;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CallRepository extends JpaRepository<Call, Long> {
    // matchingId 기준 조회
    @Query("SELECT c FROM Call c WHERE c.matching.id = :matchingId")
    List<Call> findByMatchingId(@Param("matchingId") Long matchingId);

    // 통화 상태 기준 조회
    List<Call> findByCallStatus(CallStatus callStatus);

    // 발신자(user1 기준) 조회
    @Query("SELECT c FROM Call c WHERE c.matching.user1.id = :userId")
    List<Call> findByCallerId(@Param("userId") Long userId);

    // 수신자(user2 기준) 조회
    @Query("SELECT c FROM Call c WHERE c.matching.user2.id = :userId")
    List<Call> findByReceiverId(@Param("userId") Long userId);
}