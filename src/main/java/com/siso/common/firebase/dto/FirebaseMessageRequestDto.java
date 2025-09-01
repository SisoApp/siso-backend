package com.siso.common.firebase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Firebase 메시지 전송 요청 데이터 전송 객체 (DTO)
 * 
 * 특정 사용자에게 FCM 푸시 알림을 전송할 때 사용되는 요청 데이터를 담습니다.
 * REST API의 Request Body로 사용됩니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseMessageRequestDto{

    /**
     * 대상 사용자 ID
     * 푸시 알림을 받을 사용자의 식별자 (문자열 형태)
     */
    @Schema(description = "유저ID", example = "1")
    private String userId;

    /**
     * 푸시 알림 제목
     * 사용자에게 표시될 알림의 제목
     */
    @Schema(description = "메시지 제목", example = "새로운 매칭!")
    private String title;

    /**
     * 푸시 알림 내용
     * 사용자에게 표시될 알림의 본문 내용
     */
    @Schema(description = "메시지 내용", example = "김철수님과 매칭되었습니다.")
    private String body;

    @Schema(description = "경로")
    private String url;

    @Schema(description = "읽음 상태")
    private Map<String, String> extraData;

}
