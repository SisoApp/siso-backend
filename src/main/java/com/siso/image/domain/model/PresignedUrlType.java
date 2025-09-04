package com.siso.image.domain.model;

/**
 * Presigned URL 타입 열거형
 * 
 * 이미지 Presigned URL의 유효 기간을 정의합니다.
 * 모든 타입이 5분으로 통일되어 있습니다.
 */
public enum PresignedUrlType {
    
    /** 5분 유효 (기본값) */
    DEFAULT;
    
    /**
     * 유효 기간을 분 단위로 반환
     * 
     * @return 유효 기간 (분) - 항상 5분
     */
    public int getExpirationMinutes() {
        return 5;
    }
    
    /**
     * enum 값에 따른 설명을 반환
     * 
     * @return 설명 문자열
     */
    public String getDescription() {
        return "5분 유효 (기본값)";
    }
}
