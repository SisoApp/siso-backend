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
    
    // @NotNull(message = "사용자 ID는 필수입니다") // 로그인일때 가능 - 원래 설정
    private Long userId; // 테스트용으로 NotNull 제거
}
