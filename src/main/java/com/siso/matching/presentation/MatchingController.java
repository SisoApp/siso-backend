package com.siso.matching.presentation;

import com.siso.matching.dto.MatchingResultDto;
import com.siso.matching.application.service.MatchingService;
import com.siso.matching.domain.model.MatchingRequest;
import com.siso.matching.dto.MatchingRequestResponseDto;
import com.siso.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AI 매칭 API Controller
 */
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Slf4j
public class MatchingController {

    private final MatchingService matchingService;
    private final RedisTemplate<String, MatchingResultDto> redisTemplate;

    /**
     * 매칭 요청 (비동기)
     */
    @PostMapping("/request")
    public ResponseEntity<MatchingRequestResponseDto> requestMatching(
            @AuthenticationPrincipal User user
    ) {
        log.info("Matching request: userId={}", user.getId());

        // 1. 매칭 요청 생성 및 DB 저장 (즉시)
        MatchingRequest matchingRequest = matchingService.createMatchingRequest(user);

        // 2. RabbitMQ에 이벤트 발행 (비동기)
        matchingService.publishMatchingEvent(matchingRequest);

        // 3. 즉시 응답 반환 (사용자는 기다리지 않음)
        return ResponseEntity.ok(MatchingRequestResponseDto.builder()
                .requestId(matchingRequest.getRequestId())
                .status(matchingRequest.getStatus())
                .message("매칭을 시작했습니다. 결과는 잠시 후 조회할 수 있습니다.")
                .build());
    }

    /**
     * 매칭 결과 조회 (Redis 캐시에서)
     */
    @GetMapping("/results")
    public ResponseEntity<MatchingResultDto> getMatchingResults(
            @AuthenticationPrincipal User user
    ) {
        log.info("Get matching results: userId={}", user.getId());

        // Redis 캐시에서 조회
        String cacheKey = "matching:" + user.getId();
        MatchingResultDto result = redisTemplate.opsForValue().get(cacheKey);

        if (result == null) {
            log.warn("Matching result not found in cache: userId={}", user.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        return ResponseEntity.ok(result);
    }
}
