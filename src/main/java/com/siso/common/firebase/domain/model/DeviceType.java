package com.siso.common.firebase.domain.model;

/**
 * 디바이스 타입 열거형
 * 
 * FCM 토큰을 등록할 때 사용되는 디바이스 플랫폼을 나타냅니다.
 * 푸시 알림 전송 시 플랫폼별 처리가 필요한 경우 사용됩니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
public enum DeviceType {
    /** Android 디바이스 */
    ANDROID,
    
    /** iOS 디바이스 */
    IOS
}