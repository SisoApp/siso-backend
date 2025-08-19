package com.siso.image.infrastructure.properties;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 미디어 타입 관련 프로퍼티 및 유틸리티
 * 
 * 이 클래스는 파일 확장자를 기반으로 적절한 MediaType을 결정하는 기능을 제공합니다.
 * 컨트롤러에서 직접 MediaType을 결정하지 않고 infrastructure 계층에서 처리합니다.
 */
@Component
public class MediaTypeProperties {
    
    // 지원하는 이미지 형식별 MediaType 매핑
    private static final Map<String, MediaType> MEDIA_TYPE_MAP = Map.of(
            ".jpg", MediaType.IMAGE_JPEG,
            ".jpeg", MediaType.IMAGE_JPEG,
            ".png", MediaType.IMAGE_PNG,
            ".gif", MediaType.IMAGE_GIF,
            ".webp", MediaType.parseMediaType("image/webp"),
            ".bmp", MediaType.parseMediaType("image/bmp"),
            ".svg", MediaType.parseMediaType("image/svg+xml"),
            ".ico", MediaType.parseMediaType("image/x-icon")
    );
    
    /**
     * 파일명에서 확장자를 추출하여 적절한 MediaType 반환
     * 
     * @param filename 파일명 (예: "image.jpg", "photo.png")
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
     * 파일이 이미지 파일인지 확인
     * 
     * @param filename 파일명
     * @return 이미지 파일 여부
     */
    public boolean isImageFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        String extension = extractFileExtension(filename);
        return MEDIA_TYPE_MAP.containsKey(extension);
    }
    
    /**
     * 지원되는 이미지 확장자 목록 반환
     * 
     * @return 지원되는 확장자 배열
     */
    public String[] getSupportedExtensions() {
        return MEDIA_TYPE_MAP.keySet().toArray(new String[0]);
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
