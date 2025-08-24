package com.siso.image.dto.response;

import com.siso.image.domain.model.Image;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 이미지 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageResponseDto {
    
    private Long id;
    private Long userId;
    private String path; // 이미지 접근 URL
    private String serverImageName; // 서버 파일명
    private String originalName; // 원본 파일명
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Image 엔티티를 DTO로 변환
     */
    public static ImageResponseDto fromEntity(Image image) {
        return ImageResponseDto.builder()
                .id(image.getId())
                .userId(image.getUser().getId())
                .path(image.getPath())
                .serverImageName(image.getServerImageName())
                .originalName(image.getOriginalName())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
}
