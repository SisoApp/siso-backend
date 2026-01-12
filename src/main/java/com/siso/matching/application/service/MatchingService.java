package com.siso.matching.application.service;

import com.siso.common.config.RabbitMQConfig;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.matching.application.event.MatchingRequestEvent;
import com.siso.matching.domain.model.MatchingRequest;
import com.siso.matching.domain.repository.MatchingRequestRepository;
import com.siso.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 매칭 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchingService {

    private final MatchingRequestRepository matchingRequestRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 매칭 요청 생성
     */
    public MatchingRequest createMatchingRequest(User user) {
        MatchingRequest matchingRequest = MatchingRequest.builder()
                .user(user)
                .build();

        return matchingRequestRepository.save(matchingRequest);
    }

    /**
     * RabbitMQ에 매칭 이벤트 발행
     */
    public void publishMatchingEvent(MatchingRequest request) {
        MatchingRequestEvent event = new MatchingRequestEvent(
                request.getId(),
                request.getUser().getId(),
                request.getRequestId(),
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MATCHING_EXCHANGE,
                RabbitMQConfig.MATCHING_ROUTING_KEY,
                event
        );

        log.info("Published matching event: requestId={}", request.getRequestId());
    }

    /**
     * requestId로 매칭 요청 조회
     */
    public MatchingRequest getMatchingRequestByRequestId(String requestId, Long userId) {
        return matchingRequestRepository
                .findByRequestIdAndUserId(requestId, userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.MATCHING_REQUEST_NOT_FOUND));
    }

    /**
     * 사용자의 매칭 이력 조회
     */
    public List<MatchingRequest> getMatchingHistory(Long userId) {
        return matchingRequestRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }
}
