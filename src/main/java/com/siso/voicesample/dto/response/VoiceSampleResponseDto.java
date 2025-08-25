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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static VoiceSampleResponseDto fromEntity(VoiceSample voiceSample) {
        return VoiceSampleResponseDto.builder()
                .id(voiceSample.getId())
                .userId(voiceSample.getUser().getId())
                .url(voiceSample.getUrl())
                .duration(voiceSample.getDuration()) // 음성 길이 (녹음 시 20초 제한)
                .fileSize(voiceSample.getFileSize())
                .createdAt(voiceSample.getCreatedAt())
                .updatedAt(voiceSample.getUpdatedAt())
                .build();
    }
}
