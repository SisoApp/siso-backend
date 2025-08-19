package com.siso.common.firebase.domain.model;

import com.siso.common.domain.BaseTime;
import jakarta.persistence.*;
import lombok.*;

/**
 * FCM(Firebase Cloud Messaging) 토큰 엔티티
 * 
 * 각 사용자의 디바이스별 FCM 토큰 정보를 저장합니다.
 * 사용자는 여러 디바이스를 가질 수 있으므로 한 사용자가 여러 개의 FCM 토큰을 가질 수 있습니다.
 * 토큰은 비활성화될 수 있지만 추적을 위해 삭제하지 않고 is_active 플래그로 관리합니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@Entity
@Table(name = "fcm_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmToken extends BaseTime {

    /**
     * FCM 토큰 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 ID (토큰 소유자)
     * 외래키로 사용자 테이블과 연결됩니다.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * FCM 토큰 문자열
     * Firebase에서 생성한 고유한 토큰으로 디바이스를 식별합니다.
     * 최대 500자까지 저장 가능합니다.
     */
    @Column(name = "token", nullable = false, length = 500)
    private String token;

    /**
     * 디바이스 타입 (ANDROID 또는 IOS)
     * 푸시 알림 전송 시 플랫폼별 처리를 위해 사용됩니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    /**
     * 토큰 활성화 상태
     * true: 활성화된 토큰 (푸시 알림 전송 가능)
     * false: 비활성화된 토큰 (푸시 알림 전송 불가)
     * 기본값은 true입니다.
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
