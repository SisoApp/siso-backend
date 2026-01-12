package com.siso.matching.application.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RabbitMQ로 전송할 매칭 요청 이벤트
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MatchingRequestEvent implements Serializable {
    private Long matchingRequestId;
    private Long userId;
    private String requestId;
    private LocalDateTime timestamp;
}
