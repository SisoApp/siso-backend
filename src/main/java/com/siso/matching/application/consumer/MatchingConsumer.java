package com.siso.matching.application.consumer;

import com.siso.common.config.RabbitMQConfig;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.matching.application.dto.MatchingResult;
import com.siso.matching.application.event.MatchingRequestEvent;
import com.siso.matching.application.service.MatchingAlgorithmService;
import com.siso.matching.domain.model.MatchingRequest;
import com.siso.matching.domain.model.MatchingStatus;
import com.siso.matching.domain.repository.MatchingRequestRepository;
import com.siso.user.domain.model.User;
import com.siso.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 매칭 요청 Consumer (RabbitMQ)
 * - 백그라운드에서 AI 매칭 알고리즘 실행
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingConsumer {

    private final MatchingRequestRepository matchingRequestRepository;
    private final UserRepository userRepository;
    private final MatchingAlgorithmService matchingAlgorithmService;
    private final RedisTemplate<String, MatchingResult> redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.MATCHING_QUEUE, concurrency = "3-10")
    @Transactional
    public void processMatching(MatchingRequestEvent event) {
        log.info("Processing matching from queue: requestId={}, userId={}",
                event.getRequestId(), event.getUserId());

        long startTime = System.currentTimeMillis();

        MatchingRequest matchingRequest = matchingRequestRepository
                .findById(event.getMatchingRequestId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.MATCHING_REQUEST_NOT_FOUND));

        try {
            // 1. 상태를 PROCESSING으로 변경
            matchingRequest.updateStatus(MatchingStatus.PROCESSING);
            matchingRequestRepository.save(matchingRequest);

            // 2. 사용자 조회
            User user = userRepository.findById(event.getUserId())
                    .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

            // 3. AI 매칭 알고리즘 실행 (시간이 오래 걸릴 수 있음: 3~5초)
            MatchingResult result = matchingAlgorithmService.calculateMatches(user);

            // 4. Redis에 결과 캐싱 (10분 TTL)
            String cacheKey = "matching:" + event.getUserId();
            redisTemplate.opsForValue().set(cacheKey, result, 10, TimeUnit.MINUTES);

            // 5. 매칭 완료 상태로 변경
            long processingTime = System.currentTimeMillis() - startTime;
            matchingRequest.updateStatus(MatchingStatus.COMPLETED);
            matchingRequest.updateResult(
                    result.getTotalCandidates(),
                    result.getMatches().size(),
                    (int) processingTime
            );
            matchingRequestRepository.save(matchingRequest);

            log.info("Matching completed: userId={}, matched={}/{}, time={}ms",
                    event.getUserId(), result.getMatches().size(),
                    result.getTotalCandidates(), processingTime);

        } catch (Exception e) {
            // 매칭 실패 처리
            handleMatchingFailure(matchingRequest, e);
        }
    }

    private void handleMatchingFailure(MatchingRequest matchingRequest, Exception e) {
        log.error("Matching failed: requestId={}, error={}",
                matchingRequest.getRequestId(), e.getMessage());

        matchingRequest.updateStatus(MatchingStatus.FAILED);
        matchingRequestRepository.save(matchingRequest);
    }
}
