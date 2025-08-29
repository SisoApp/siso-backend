package com.siso.user.dto.response;

import com.siso.user.domain.model.*;
import lombok.*;

import java.util.List;

/**
 * 매칭용 프로필 응답 DTO
 * 
 * 매칭 화면에서 필요한 핵심 정보만 포함
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchingProfileResponseDto {
    private Long userId;                    // 유저 아이디
    private String nickname;                // 닉네임
    private int age;                        // 나이
    private String location;                // 사는 지역
    private List<String> interests;         // 취향 (관심사)
    private String introduce;               // 소개글
    private List<String> imageUrls;         // 사진 URL 배열
    private String voiceSampleUrl;// 음성 샘플 URL (추후 구현)
    private Enum presenseStatus;
    
    /**
     * UserProfile 엔티티를 매칭용 DTO로 변환
     */
    public static MatchingProfileResponseDto fromUserProfile(
            UserProfile profile, 
            List<String> interests, 
            List<String> imageUrls) {
        
        return MatchingProfileResponseDto.builder()
                .userId(profile.getUser().getId())
                .nickname(profile.getNickname())
                .age(profile.getAge())
                .location(profile.getLocation())
                .interests(interests)
                .introduce(profile.getIntroduce())
                .imageUrls(imageUrls)
                .voiceSampleUrl(null) // 추후 음성 샘플 기능 구현 시 수정
                .presenseStatus(profile.getUser().getPresenceStatus())
                .build();
    }
}
