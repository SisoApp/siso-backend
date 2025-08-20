package com.siso.image.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 이미지 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequestDto {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
}
