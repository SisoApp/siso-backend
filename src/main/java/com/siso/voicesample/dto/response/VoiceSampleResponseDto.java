package com.siso.voicesample.dto.response;

import com.siso.voicesample.domain.model.VoiceSample;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceSampleResponseDto {
    private Long id;
    private Long userId;
    private String url;
    private Integer duration; // 음성 길이 (초 단위, 최대 20초)
    private Integer fileSize; // 파일 크기 (바이트)
    
    // === Presigned URL 정보 ===
    private String presignedUrl; // 현재 유효한 Presigned URL
    private LocalDateTime presignedUrlExpiresAt; // Presigned URL 만료 시간
    private boolean presignedUrlValid; // Presigned URL 유효성 여부
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static VoiceSampleResponseDto fromEntity(VoiceSample voiceSample) {
        // Presigned URL 유효성 확인
        boolean isUrlValid = voiceSample.isPresignedUrlValid();
        
        return VoiceSampleResponseDto.builder()
                .id(voiceSample.getId())
                .userId(voiceSample.getUser().getId())
                .url(voiceSample.getUrl())
                .duration(voiceSample.getDuration()) // 음성 길이 (녹음 시 20초 제한)
                .fileSize(voiceSample.getFileSize())
                .presignedUrl(isUrlValid ? voiceSample.getPresignedUrl() : null)  // 만료된 경우 null
                .presignedUrlExpiresAt(isUrlValid ? voiceSample.getPresignedUrlExpiresAt() : null)  // 만료된 경우 null
                .presignedUrlValid(isUrlValid)  // 실제 유효성 여부
                .createdAt(voiceSample.getCreatedAt())
                .updatedAt(voiceSample.getUpdatedAt())
                .build();
    }
}
