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

    @Builder
    public VoiceSample(User user, String url, Integer duration, Integer fileSize) {
        this.user = user;
        user.linkVoiceSample(this);
        this.url = url;
        this.duration = duration;
        this.fileSize = fileSize;
    }
}
