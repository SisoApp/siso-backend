package com.siso.voicesample.infrastructure.properties;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 음성 미디어 타입 관련 프로퍼티 및 유틸리티
 * 
 * 이 클래스는 음성 파일 확장자를 기반으로 적절한 MediaType을 결정하는 기능을 제공합니다.
 * 컨트롤러에서 직접 MediaType을 결정하지 않고 infrastructure 계층에서 처리합니다.
 * 
 * VoiceMediaTypeProperties = "HTTP 응답 도우미"
 */
@Component
public class VoiceMediaTypeProperties {
    
    // 지원하는 음성 형식별 MediaType 매핑
    private static final Map<String, MediaType> MEDIA_TYPE_MAP = Map.of(
            ".mp3", MediaType.parseMediaType("audio/mpeg"),
            ".wav", MediaType.parseMediaType("audio/wav"),
            ".m4a", MediaType.parseMediaType("audio/mp4"),
            ".aac", MediaType.parseMediaType("audio/aac"),
            ".ogg", MediaType.parseMediaType("audio/ogg"),
            ".webm", MediaType.parseMediaType("audio/webm"),
            ".flac", MediaType.parseMediaType("audio/flac")
    );
    
    /**
     * 파일명에서 확장자를 추출하여 적절한 MediaType 반환
     * 음성 파일 재생을 위한 올바른 Content-Type 설정
     * 
     * @param filename 파일명 (예: "voice.mp3", "sample.wav")
     * @return 해당 파일의 MediaType
     */
    public MediaType determineContentType(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        
        // 파일 확장자 추출
        String extension = extractFileExtension(filename);
        
        // 매핑에서 MediaType 찾기
        MediaType mediaType = MEDIA_TYPE_MAP.get(extension);
        
        // 매핑되지 않은 경우 기본값 반환
        return mediaType != null ? mediaType : MediaType.APPLICATION_OCTET_STREAM;
    }
    
    /**
     * 파일이 음성 파일인지 확인
     * 
     * @param filename 파일명
     * @return 음성 파일 여부
     */
    public boolean isAudioFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        String extension = extractFileExtension(filename);
        return MEDIA_TYPE_MAP.containsKey(extension);
    }
    
    /**
     * 지원되는 음성 확장자 목록 반환
     * 
     * @return 지원되는 확장자 배열
     */
    public String[] getSupportedExtensions() {
        return MEDIA_TYPE_MAP.keySet().toArray(new String[0]);
    }
    
    /**
     * 특정 확장자가 지원되는지 확인
     * 
     * @param extension 확장자 (점 포함 또는 미포함 모두 지원)
     * @return 지원 여부
     */
    public boolean isSupportedExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return false;
        }
        
        String normalizedExtension = extension.startsWith(".") ? 
            extension.toLowerCase() : 
            "." + extension.toLowerCase();
            
        return MEDIA_TYPE_MAP.containsKey(normalizedExtension);
    }
    
    /**
     * URL에서 파일명만 추출
     * 
     * 음성 파일 뷰어에서 사용되는 유틸리티 메서드입니다.
     * URL 경로에서 마지막 슬래시 이후의 파일명을 추출합니다.
     * 
     * @param fileUrl 파일 URL (예: http://localhost:8080/api/voice-samples/files/20250101_123456_abc123.mp3)
     * @return 파일명 (예: 20250101_123456_abc123.mp3), 추출 실패 시 null
     */
    public String extractFilenameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return null;
        }
        
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < fileUrl.length() - 1) {
            return fileUrl.substring(lastSlashIndex + 1);
        }
        return null;
    }
    
    // ===================== 내부 헬퍼 메서드 =====================
    
    /**
     * 파일명에서 확장자 추출
     * 
     * @param filename 파일명
     * @return 확장자 (소문자, 점 포함)
     */
    private String extractFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        return filename.substring(lastDotIndex).toLowerCase();
    }
}
