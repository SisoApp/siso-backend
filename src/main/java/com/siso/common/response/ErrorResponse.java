package com.siso.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 표준화된 에러 응답 DTO
 * - 모든 API 에러가 동일한 형식으로 반환됨을 보장
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;
    private final List<FieldError> fieldErrors;

    /**
     * 필드 검증 에러 상세 정보
     */
    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final Object rejectedValue;
        private final String message;
    }

    /**
     * 기본 에러 응답 생성
     */
    public static ErrorResponse of(int status, String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 필드 검증 에러 응답 생성
     */
    public static ErrorResponse withFieldErrors(
            int status,
            String errorCode,
            String message,
            String path,
            List<FieldError> fieldErrors
    ) {
        return ErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
    }
}
