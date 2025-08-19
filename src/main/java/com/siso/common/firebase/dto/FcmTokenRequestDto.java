package com.siso.common.firebase.dto;

import com.siso.common.firebase.domain.model.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FCM 토큰 요청 데이터 전송 객체 (DTO)
 * 
 * 클라이언트에서 FCM 토큰을 등록하거나 해제할 때 사용되는 요청 데이터를 담습니다.
 * REST API의 Request Body로 사용됩니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequestDto {

    /**
     * 사용자 ID
     * FCM 토큰을 등록하거나 해제할 대상 사용자의 식별자
     */
    @Schema(description = "유저ID", example = "1")
    private Long userId;

    /**
     * FCM 토큰 문자열
     * Firebase에서 생성한 고유한 디바이스 식별 토큰
     */
    @Schema(description = "FCM 토큰", example = "dQw4w9WgXcQ:APA91bGHXQBhizGnubvizQoUHR...")
    private String token;

    /**
     * 디바이스 타입
     * 토큰이 생성된 플랫폼 (ANDROID 또는 IOS)
     */
    @Schema(description = "디바이스 타입", example = "ANDROID")
    private DeviceType deviceType;
}
