package com.siso.image.domain.model;

import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 이미지 엔티티
 * 
 * 한 유저당 최대 5개까지 이미지 저장 가능
 * 이미지 업로드, 수정, 삭제 기능 제공
 * Presigned URL을 서버에서 관리하여 클라이언트 API 호출 최소화
 */
@Entity
@Table(name = "images")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "path", length = 255)
    private String path; // S3 원본 경로

    @Column(name = "server_image_name", length = 255, nullable = false)
    private String serverImageName; // 서버에 저장된 파일명

    @Column(name = "original_name", length = 255, nullable = false)
    private String originalName; // 원본 파일명

    // === Presigned URL 관리 필드들 ===
    @Column(name = "presigned_url", length = 500)
    private String presignedUrl; // 현재 유효한 Presigned URL
    
    @Column(name = "presigned_url_expires_at")
    private LocalDateTime presignedUrlExpiresAt; // Presigned URL 만료 시간
    
    @Column(name = "presigned_url_type", length = 20)
    @Enumerated(EnumType.STRING)
    private PresignedUrlType presignedUrlType; // Presigned URL 타입 (SHORT, MEDIUM, LONG)

    @Builder
    public Image(User user, String path, String serverImageName, String originalName) {
        this.user = user;
        this.path = path;
        this.serverImageName = serverImageName;
        this.originalName = originalName;
    }

    public void updateImage(String path, String serverImageName, String originalName) {
        this.path = path;
        this.serverImageName = serverImageName;
        this.originalName = originalName;
        // 이미지가 변경되면 Presigned URL도 초기화
        this.presignedUrl = null;
        this.presignedUrlExpiresAt = null;
        this.presignedUrlType = null;
    }

    /**
     * Presigned URL 업데이트
     */
    public void updatePresignedUrl(String presignedUrl, LocalDateTime expiresAt, PresignedUrlType type) {
        this.presignedUrl = presignedUrl;
        this.presignedUrlExpiresAt = expiresAt;
        this.presignedUrlType = type;
    }

    /**
     * Presigned URL이 유효한지 확인
     * 
     * 현재 시간과 만료 시간을 비교하여 정확한 만료 여부를 판단합니다.
     * 밀리초 단위까지 고려하여 정확한 시간 비교를 수행합니다.
     * 
     * 보안을 위해 만료 1분 전부터는 유효하지 않은 것으로 처리합니다.
     */
    public boolean isPresignedUrlValid() {
        if (presignedUrl == null || presignedUrlExpiresAt == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        // 보안을 위해 만료 1분 전부터는 유효하지 않은 것으로 처리
        LocalDateTime safetyMargin = presignedUrlExpiresAt.minusMinutes(1);
        boolean isValid = now.isBefore(safetyMargin);
        
        // 디버깅을 위한 로그 (필요시 주석 해제)
        // System.out.println("=== Presigned URL 유효성 체크 ===");
        // System.out.println("현재 시간: " + now);
        // System.out.println("만료 시간: " + presignedUrlExpiresAt);
        // System.out.println("안전 마진: " + safetyMargin);
        // System.out.println("유효 여부: " + isValid);
        
        return isValid;
    }

    /**
     * Presigned URL 접근 가능 여부 확인
     * 
     * @return 접근 가능하면 true, 만료되었으면 false
     */
    public boolean canAccessWithPresignedUrl() {
        return isPresignedUrlValid();
    }

    /**
     * 만료된 Presigned URL인지 확인
     * 
     * @return 만료되었으면 true, 유효하면 false
     */
    public boolean isPresignedUrlExpired() {
        return !isPresignedUrlValid();
    }
}
