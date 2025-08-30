package com.siso.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    USER_NOT_FOUND_OR_DELETED(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없거나 삭제된 계정입니다."),
    UNAUTHROIZED(HttpStatus.UNAUTHORIZED, "인증되지 않는 사용자"),

    // 사용자 프로필
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."),
    USER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자의 프로필을 찾을 수 없습니다."),

    // Refresh Token
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),

    // OAuth2
    OAUTH2_EMAIL_NOT_FOUND(HttpStatus.UNAUTHORIZED, "이메일 제공이 되지 않은 상태입니다."),
    OAUTH2_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 OAuth2 토큰입니다."),

    // 소셜 로그인 관련 에러
    UNSUPPORTED_SOCIAL_LOGIN(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),

    // 관심사
    TOO_MANY_INTERESTS(HttpStatus.NOT_FOUND, "5개를 초과 하여 선택할 수 없습니다."),
    NO_INTERESTS_SELECTED(HttpStatus.NOT_FOUND,"최소 한 개 이상 선택 해야 합니다."),

    // 통화
    CALL_NOT_FOUND(HttpStatus.NOT_FOUND, "통화 정보가 없습니다."),
    USER_IN_CALL(HttpStatus.FORBIDDEN, "사용자가 이미 통화 중입니다."),

    // 채팅방
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHATROOM_EMPTY(HttpStatus.NOT_FOUND, "채팅방에 메시지가 없습니다."),

    // 채팅 메시지 관련
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),
    NOT_YOUR_MESSAGE(HttpStatus.FORBIDDEN, "자신의 메시지만 수정/삭제할 수 있습니다."),
    MESSAGE_LIMIT_EXCEEDED(HttpStatus.FORBIDDEN, "메시지 전송 횟수 제한을 초과했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 채팅방 멤버
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 멤버를 찾을 수 없습니다."),
    NOT_CHATROOM_MEMBER(HttpStatus.FORBIDDEN, "채팅방에 속한 멤버가 아닙니다."),

    // 통화 리뷰
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "통화 리뷰가 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 평가를 작성하셨습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // 신고
    REPORTER_NOT_FOUND(HttpStatus.NOT_FOUND, "신고자를 찾을 수 없습니다."),
    REPORTED_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "피신고자를 찾을 수 없습니다."),

    // 이미지
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
    IMAGE_EMPTY(HttpStatus.NOT_FOUND, "이미지가 비어 있습니다"),
    IMAGE_UPLOAD_PERSIST_FAIL(HttpStatus.INTERNAL_SERVER_ERROR,"이미지를 업로드했지만 저장소에 저장되지 않았습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "I004 이미지 업로드에 실패했습니다."),
    IMAGE_MAX_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "사용자당 최대 이미지 개수를 초과했습니다."),
    IMAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 이미지에 접근할 권한이 없습니다."),
    INVALID_IMAGE_FILE(HttpStatus.FORBIDDEN, "유효하지 않는 파일 입니다."),
    
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
