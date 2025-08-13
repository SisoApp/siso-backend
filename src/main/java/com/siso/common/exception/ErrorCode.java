package com.siso.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),

    // 관심사
    TOO_MANY_INTERESTS(HttpStatus.NOT_FOUND, "5개를 초과 하여 선택할 수 없습니다."),
    NO_INTERESTS_SELECTED(HttpStatus.NOT_FOUND,"최소 한 개 이상 선택 해야 합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
