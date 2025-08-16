package com.siso.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),

    // Refresh Token
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "DB에 리프레시 토큰이 없거나 유효하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다."),

    // OAuth2
    OAUTH2_PHONE_NUMBER_REQUIRED(HttpStatus.UNAUTHORIZED, "핸드폰 번호 제공 동의가 필요합니다."),
    OAUTH2_PHONE_NUMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "핸드폰 번호 제공이 되지 않은 상태입니다."),

    // 소셜 로그인 관련 에러
    UNSUPPORTED_SOCIAL_LOGIN(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),

    // 관심사
    TOO_MANY_INTERESTS(HttpStatus.NOT_FOUND, "5개를 초과 하여 선택할 수 없습니다."),
    NO_INTERESTS_SELECTED(HttpStatus.NOT_FOUND,"최소 한 개 이상 선택 해야 합니다."),

    // 매칭
    MATCHING_NOT_FOUND(HttpStatus.NOT_FOUND,"매칭 정보가 없습니다."),
  
    // 이미지
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
    IMAGE_FILE_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다."),
    IMAGE_INVALID_FILENAME(HttpStatus.BAD_REQUEST, "파일명이 올바르지 않습니다."),
    IMAGE_UNSUPPORTED_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    IMAGE_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "파일 크기가 너무 큽니다."),
    IMAGE_MAX_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "사용자당 최대 이미지 개수를 초과했습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    IMAGE_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지 파일을 찾을 수 없습니다."),
    IMAGE_INVALID_PATH(HttpStatus.BAD_REQUEST, "잘못된 파일 경로입니다."),
    IMAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 이미지에 접근할 권한이 없습니다."),
    
    // 음성 샘플
    VOICE_SAMPLE_NOT_FOUND(HttpStatus.NOT_FOUND, "음성 샘플을 찾을 수 없습니다."),
    VOICE_SAMPLE_FILE_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 음성 파일이 없습니다."),
    VOICE_SAMPLE_INVALID_FILENAME(HttpStatus.BAD_REQUEST, "음성 파일명이 올바르지 않습니다."),
    VOICE_SAMPLE_UNSUPPORTED_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 음성 파일 형식입니다."),
    VOICE_SAMPLE_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "음성 파일 크기가 너무 큽니다."),
    VOICE_SAMPLE_MAX_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "사용자당 최대 음성 샘플 개수를 초과했습니다."),
    VOICE_SAMPLE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "음성 파일 업로드에 실패했습니다."),
    VOICE_SAMPLE_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "음성 파일을 찾을 수 없습니다."),
    VOICE_SAMPLE_INVALID_PATH(HttpStatus.BAD_REQUEST, "잘못된 음성 파일 경로입니다."),
    VOICE_SAMPLE_FILE_TOO_LONG(HttpStatus.BAD_REQUEST, "음성 파일 길이가 너무 깁니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
