package com.siso.call.domain.repository;

import com.siso.call.domain.model.Call;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CallRepository extends JpaRepository<Call, Long> {
    // 발신자 조회
    List<Call> findByCallerId(Long callerId);

    // 수신자 조회
    List<Call> findByReceiverId(Long receiverId);

    // 최초 통화인지 판별
    Optional<Call> findFirstByCallerIdAndReceiverIdOrderByStartTimeAsc(Long callerId, Long receiverId);
}