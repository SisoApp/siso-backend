package com.siso.voicesample.domain.model;

import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "voice_samples")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoiceSample extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "url", length = 255) // 주소
    private String url;
    
    @Column(name = "duration") // 음성 길이 (초 단위, 최대 20초)
    private Integer duration;
    
    @Column(name = "file_size", columnDefinition = "INT COMMENT '바이트로 받기'")
    private Integer fileSize;

    // === Presigned URL 관리 필드들 ===
    @Column(name = "presigned_url", length = 500)
    private String presignedUrl; // 현재 유효한 Presigned URL
    
    @Column(name = "presigned_url_expires_at")
    private java.time.LocalDateTime presignedUrlExpiresAt; // Presigned URL 만료 시간

    @Builder
    public VoiceSample(User user, String url, Integer duration, Integer fileSize) {
        this.user = user;
        // 양방향 연관 관계 설정
        user.linkVoiceSample(this);
        this.url = url;
        this.duration = duration;
        this.fileSize = fileSize;
    }

    /**
     * Presigned URL 업데이트
     */
    public void updatePresignedUrl(String presignedUrl, java.time.LocalDateTime expiresAt) {
        this.presignedUrl = presignedUrl;
        this.presignedUrlExpiresAt = expiresAt;
    }

    /**
     * Presigned URL이 유효한지 확인
     * 
     * 보안을 위해 만료 1분 전부터는 유효하지 않은 것으로 처리합니다.
     */
    public boolean isPresignedUrlValid() {
        if (presignedUrl == null || presignedUrlExpiresAt == null) {
            return false;
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        // 보안을 위해 만료 1분 전부터는 유효하지 않은 것으로 처리
        java.time.LocalDateTime safetyMargin = presignedUrlExpiresAt.minusMinutes(1);
        return now.isBefore(safetyMargin);
    }

    /**
     * Presigned URL 접근 가능 여부 확인
     */
    public boolean canAccessWithPresignedUrl() {
        return isPresignedUrlValid();
    }

    /**
     * 만료된 Presigned URL인지 확인
     */
    public boolean isPresignedUrlExpired() {
        return !isPresignedUrlValid();
    }
}
