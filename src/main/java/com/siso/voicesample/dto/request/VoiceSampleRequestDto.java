package com.siso.voicesample.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceSampleRequestDto {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
}
