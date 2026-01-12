package com.siso.image.infrastructure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 이미지 관련 설정 프로퍼티
 * 
 * application.yml의 app.image 설정을 타입 안전하게 바인딩
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.image")
public class ImageProperties {
    
    /**
     * 이미지 파일 저장 디렉토리 경로
     */
    @NotBlank(message = "업로드 디렉토리는 필수입니다")
    private String uploadDir = "/images";
    
    /**
     * 파일 접근을 위한 기본 URL
     */
    @NotBlank(message = "기본 URL은 필수입니다")
     private String baseUrl = "https://13.124.11.3:8080"; // 배포시에
//    private String baseUrl = "https://localhost:8080";
    /**
     * 파일 크기 제한 (바이트 단위)
     */
    @Min(value = 1024, message = "파일 크기 제한은 최소 1KB 이상이어야 합니다")
    private long maxFileSize = 10485760L; // 10MB
    
    /**
     * 지원하는 파일 형식들
     */
    @NotNull(message = "지원 파일 형식은 필수입니다")
    private List<String> supportedFormats = List.of("jpg", "jpeg", "png", "gif", "webp");
    
    /**
     * 사용자당 최대 이미지 개수
     */
    @Min(value = 1, message = "최대 이미지 개수는 1개 이상이어야 합니다")
    private int maxImagesPerUser = 5;
    
    /**
     * 파일 크기를 MB 단위로 반환
     */
    public long getMaxFileSizeInMB() {
        return maxFileSize / (1024 * 1024);
    }
    
    /**
     * 지원 형식을 콤마로 구분된 문자열로 반환
     */
    public String getSupportedFormatsAsString() {
        return String.join(",", supportedFormats);
    }
    
    /**
     * 특정 파일 확장자가 지원되는지 확인
     */
    public boolean isSupportedFormat(String extension) {
        if (extension == null) return false;
        String cleanExtension = extension.toLowerCase().replace(".", "");
        return supportedFormats.contains(cleanExtension);
    }
}
