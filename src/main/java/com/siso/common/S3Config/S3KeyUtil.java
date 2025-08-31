package com.siso.common.S3Config;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * S3 키 관리 유틸리티 클래스
 * 
 * S3 객체 키 생성 및 관리 기능을 제공합니다.
 */
@Component
public class S3KeyUtil {

    private static final String PREFIX = "images/"; // S3 폴더 prefix

    /**
     * UUID 기반 고유 파일명 생성
     * 
     * @param originalName 원본 파일명
     * @return UUID + 원본 파일명
     */
    public String generateUuidName(String originalName) {
        String safe = Optional.ofNullable(originalName).orElse("file").replaceAll("[^a-zA-Z0-9._-]", "_");
        return UUID.randomUUID() + "-" + safe;
    }

    /**
     * S3 객체 키 생성
     * 
     * @param userId 사용자 ID (폴더 구조에 사용)
     * @param serverFileName 서버 파일명
     * @return S3 객체 키
     */
    public String buildKey(Long userId, String serverFileName) {
        if (userId != null) {
            return PREFIX + userId + "/" + serverFileName;
        }
        return PREFIX + serverFileName;
    }

    /**
     * S3 URL에서 키 추출
     * 
     * @param keyOrUrl S3 키 또는 URL
     * @return S3 키
     */
    public String extractKey(String keyOrUrl) {
        if (keyOrUrl == null) return null;
        // 이미 key라면 그대로 리턴
        if (!keyOrUrl.startsWith("http")) return keyOrUrl;
        // https://{bucket}.s3.{region}.amazonaws.com/{key}
        int idx = keyOrUrl.indexOf(".amazonaws.com/");
        if (idx == -1) return keyOrUrl; // 예상치 못한 포맷이면 원본 반환
        return keyOrUrl.substring(idx + ".amazonaws.com/".length());
    }
}