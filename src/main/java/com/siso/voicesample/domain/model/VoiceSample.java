package com.siso.voicesample.domain.model;

import com.siso.common.domain.BaseTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Entity
@Table(name = "voice_samples")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceSample extends BaseTime {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // @Column(name = "user_id", nullable = false) // 로그인일때 가능 - 원래 설정
    @Column(name = "user_id", nullable = true, columnDefinition = "BIGINT DEFAULT 1") // 테스트용으로 nullable 허용하고 기본값 설정
    private Long userId;
    
    @Column(name = "url", length = 255) // 주소
    private String url;
    
    @Column(name = "duration") // 음성 길이 (초 단위, 최대 20초)
    private Integer duration;
    
    @Column(name = "file_size", columnDefinition = "INT COMMENT '바이트로 받기'")
    private Integer fileSize;
}
