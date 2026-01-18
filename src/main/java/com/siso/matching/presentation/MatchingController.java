package com.siso.matching.presentation;

import com.siso.matching.dto.MatchingResultDto;
import com.siso.matching.application.service.MatchingService;
import com.siso.matching.domain.model.MatchingRequest;
import com.siso.matching.dto.MatchingRequestResponseDto;
import com.siso.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Matching", description = "AI 기반 사용자 매칭 API")
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class MatchingController {

    private final MatchingService matchingService;
    private final RedisTemplate<String, MatchingResultDto> redisTemplate;

    @Operation(
            summary = "AI 매칭 요청",
            description = "AI 알고리즘을 사용하여 사용자와 매칭되는 후보들을 찾습니다. " +
                    "비동기로 처리되며, 결과는 Redis 캐시에 저장됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "매칭 요청 성공",
                    content = @Content(schema = @Schema(implementation = MatchingRequestResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 프로필 없음", content = @Content)
    })
    @PostMapping("/request")
    public ResponseEntity<MatchingRequestResponseDto> requestMatching(
            @Parameter(hidden = true) @AuthenticationPrincipal User user
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

    @Operation(
            summary = "매칭 결과 조회",
            description = "AI 매칭 결과를 Redis 캐시에서 조회합니다. " +
                    "매칭이 아직 완료되지 않았다면 404를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MatchingResultDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "매칭 결과 없음 (아직 처리 중이거나 만료됨)", content = @Content)
    })
    @GetMapping("/results")
    public ResponseEntity<MatchingResultDto> getMatchingResults(
            @Parameter(hidden = true) @AuthenticationPrincipal User user
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
