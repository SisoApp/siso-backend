package com.siso.call.domain.repository;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.model.CallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CallRepository extends JpaRepository<Call, Long> {
    // 발신자(User) 기준 통화 조회
    @Query("SELECT c FROM Call c WHERE c.matching.sender.id = :senderId")
    List<Call> findBySenderId(@Param("senderId") Long senderId);

    // 수신자(User) 기준 통화 조회
    @Query("SELECT c FROM Call c WHERE c.matching.receiver.id = :receiverId")
    List<Call> findByReceiverId(@Param("receiverId") Long receiverId);

    // callStatus 기준 조회
    List<Call> findByCallStatus(CallStatus callStatus);
}