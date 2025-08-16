package com.siso.voicesample.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * 음성 파일 처리 결과를 담는 도메인 모델
 * <p>
 * 음성 파일 처리 후 생성된 URL, 추출된 duration, 파일 크기를 포함합니다.
 * 이 클래스는 음성 파일 처리 로직의 결과를 명확하게 전달하고,
 * 관련 데이터를 캡슐화하여 응집도를 높입니다.
 * 
 * VoiceFileProcessResult = "음성 파일 처리 결과 담당자"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceFileProcessResult {
    /** 생성된 파일 접근 URL */
    private String fileUrl;
    /** 추출된 음성 길이 (초 단위) */
    private Integer duration;
    /** 파일 크기 (바이트) */
    private Integer fileSize;

    /**
     * 정적 팩토리 메서드
     *
     * @param fileUrl  파일 접근 URL
     * @param duration 음성 길이 (초)
     * @param fileSize 파일 크기 (바이트)
     * @return VoiceFileProcessResult 인스턴스
     */
    public static VoiceFileProcessResult of(String fileUrl, Integer duration, Integer fileSize) {
        return new VoiceFileProcessResult(fileUrl, duration, fileSize);
    }

    /**
     * 파일 처리 결과가 유효한지 확인
     * (모든 필수 필드가 null이 아닌지)
     *
     * @return 유효성 여부
     */
    public boolean isValid() {
        return fileUrl != null && !fileUrl.isEmpty() &&
                duration != null && duration > 0 &&
                fileSize != null && fileSize > 0;
    }

    /**
     * 파일 URL에서 파일명 추출
     *
     * @return 파일명 (예: "20250101_123456_abc123.mp3"), 추출 실패 시 빈 문자열
     */
    public String extractFileName() {
        return Optional.ofNullable(fileUrl)
                .filter(url -> url.contains("/"))
                .map(url -> url.substring(url.lastIndexOf("/") + 1))
                .orElse("");
    }

    /**
     * 파일 URL에서 확장자 추출
     *
     * @return 파일 확장자 (예: ".mp3"), 없으면 빈 문자열
     */
    public String getFileExtension() {
        String fileName = extractFileName();
        return Optional.ofNullable(fileName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse("");
    }

    /**
     * 파일 크기를 MB 단위로 반환
     *
     * @return 파일 크기 (MB), null인 경우 0.0
     */
    public double getFileSizeInMB() {
        return fileSize != null ? fileSize / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 음성 길이를 분:초 형태로 반환
     *
     * @return 음성 길이 (예: "1:23"), null인 경우 "0:00"
     */
    public String getFormattedDuration() {
        if (duration == null || duration <= 0) {
            return "0:00";
        }
        
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
