package com.siso.image.domain.model;

import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 이미지 엔티티
 * 
 * 한 유저당 최대 5개까지 이미지 저장 가능
 * 이미지 업로드, 수정, 삭제 기능 제공
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

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "path", length = 255)
    private String path; // 이미지 파일 경로

    @Column(name = "server_image_name", length = 255, nullable = false)
    private String serverImageName; // 서버에 저장된 파일명

    @Column(name = "original_name", length = 255, nullable = false)
    private String originalName; // 원본 파일명

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
    }
}
