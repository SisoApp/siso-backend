package com.siso.image.dto.response;

import com.siso.image.domain.model.Image;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 이미지 응답 DTO
 * Presigned URL 정보를 포함하여 클라이언트 API 호출 최소화
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageResponseDto {
    private Long id;
    private Long userId;
    private String path; // S3 원본 경로
    private String serverImageName; // 서버 파일명
    private String originalName; // 원본 파일명
    
    // === Presigned URL 정보 ===
    private String presignedUrl; // 현재 유효한 Presigned URL
    private LocalDateTime presignedUrlExpiresAt; // Presigned URL 만료 시간
    private String presignedUrlType; // Presigned URL 타입
    private boolean presignedUrlValid; // Presigned URL 유효성 여부
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Image 엔티티를 DTO로 변환
     * 
     * 만료된 Presigned URL은 null로 설정하여 클라이언트가 사용할 수 없도록 합니다.
     * 클라이언트는 presignedUrlValid 필드를 확인하여 URL 사용 가능 여부를 판단할 수 있습니다.
     */
    public static ImageResponseDto fromEntity(Image image) {
        // Presigned URL 유효성 확인
        boolean isUrlValid = image.isPresignedUrlValid();
        
        return ImageResponseDto.builder()
                .id(image.getId())
                .userId(image.getUser().getId())
                .path(image.getPath())
                .serverImageName(image.getServerImageName())
                .originalName(image.getOriginalName())
                .presignedUrl(isUrlValid ? image.getPresignedUrl() : null)  // 만료된 경우 null
                .presignedUrlExpiresAt(isUrlValid ? image.getPresignedUrlExpiresAt() : null)  // 만료된 경우 null
                .presignedUrlType(isUrlValid && image.getPresignedUrlType() != null ? 
                        image.getPresignedUrlType().name() : null)  // 만료된 경우 null
                .presignedUrlValid(isUrlValid)  // 실제 유효성 여부
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
}
