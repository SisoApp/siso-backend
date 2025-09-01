package com.siso.call.domain.repository;

import com.siso.call.domain.model.Call;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CallRepository extends JpaRepository<Call, Long> {
    // 발신자 조회
    List<Call> findByCallerId(Long callerId);

    // 수신자 조회
    List<Call> findByReceiverId(Long receiverId);
}