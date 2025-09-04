package com.siso.user.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import com.siso.user.domain.model.UserInterest;
import com.siso.user.domain.repository.UserProfileRepository;
import com.siso.user.domain.repository.UserInterestRepository;
import com.siso.user.dto.response.UserInterestResponseDto;
import com.siso.user.dto.response.FilteredUserResponseDto;
import com.siso.image.domain.repository.ImageRepository;
import com.siso.image.dto.response.ImageResponseDto;
import com.siso.image.application.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.siso.user.dto.response.MatchingProfileResponseDto;

/**
 * 사용자 필터링 및 매칭 서비스
 * 
 * Presigned URL을 활용한 효율적인 이미지 처리로
 * 클라이언트 API 호출을 최소화합니다.
 */
@Service
@RequiredArgsConstructor
public class UserFilterService {
    private final UserProfileRepository userProfileRepository;
    private final UserInterestRepository userInterestRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService; // Presigned URL 자동 관리용

    @Transactional(readOnly = true)
    public List<FilteredUserResponseDto> getFilteredUsers(User user) {
        // 사용자와 사용자 프로필 조회
        Long userId = user.getId();
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // PreferenceSex에 해당하는 모든 사용자를 관심사 겹치는 순으로 조회
        List<UserProfile> filteredProfiles = userProfileRepository.findFilteredUsersByPreferenceSex(
                userId,
                userId, // 자기 자신 제외
                userProfile.getPreferenceSex() != null ? userProfile.getPreferenceSex().name() : null,
                userProfile.getReligion() != null ? userProfile.getReligion().name() : null,
                userProfile.isSmoke(),
                userProfile.getLocation() != null ? userProfile.getLocation() : null,
                userProfile.getDrinkingCapacity() != null ? userProfile.getDrinkingCapacity().name() : null,
                userProfile.getAge()
        );

        return filteredProfiles.stream()
                .map(profile -> {
                    // 각 프로필의 관심사 조회
                    List<UserInterest> interests = userInterestRepository.findByUserId(profile.getUser().getId());
                    List<UserInterestResponseDto> interestDtos = interests.stream()
                            .map(interest -> new UserInterestResponseDto(interest.getInterest()))
                            .collect(Collectors.toList());

                    // 각 프로필의 이미지 조회 (Presigned URL 자동 생성 포함)
                    List<ImageResponseDto> profileImages = imageService.getImagesByUserId(profile.getUser().getId());

                    // 공통 관심사 개수 계산
                    List<UserInterest> myInterests = userInterestRepository.findByUserId(userId);
                    int commonInterestsCount = (int) interests.stream()
                            .filter(otherInterest -> myInterests.stream()
                                    .anyMatch(myInterest -> myInterest.getInterest().equals(otherInterest.getInterest())))
                            .count();

                    return FilteredUserResponseDto.builder()
                            .userId(profile.getUser().getId())
                            .profileId(profile.getId())
                            .nickname(profile.getNickname())
                            .introduce(profile.getIntroduce())
                            .age(profile.getAge())
                            .sex(profile.getSex())
                            .location(profile.getLocation())
                            .religion(profile.getReligion())
                            .smoke(profile.isSmoke())
                            .drinkingCapacity(profile.getDrinkingCapacity())
                            .interests(interestDtos)
                            .profileImages(profileImages)
                            .commonInterestsCount(commonInterestsCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 매칭용 프로필 조회 (무한 스크롤 지원)
     * 
     * Presigned URL을 활용하여 이미지를 효율적으로 처리합니다.
     * 
     * @param user 현재 사용자
     * @param page 페이지 번호 (0부터 시작)
     * @param count 조회할 프로필 개수
     * @return 매칭용 프로필 리스트
     */
    @Transactional(readOnly = true)
    public List<MatchingProfileResponseDto> getMatchingProfiles(User user, int page, int count) {
        // 사용자와 사용자 프로필 조회
        Long userId = user.getId();
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_PROFILE_NOT_FOUND));

        // PreferenceSex에 해당하는 모든 사용자를 관심사 겹치는 순으로 조회
        List<UserProfile> filteredProfiles = userProfileRepository.findFilteredUsersByPreferenceSex(
                userId,
                userId, // 자기 자신 제외
                userProfile.getPreferenceSex() != null ? userProfile.getPreferenceSex().name() : null,
                userProfile.getReligion() != null ? userProfile.getReligion().name() : null,
                userProfile.isSmoke(),
                userProfile.getLocation() != null ? userProfile.getLocation() : null,
                userProfile.getDrinkingCapacity() != null ? userProfile.getDrinkingCapacity().name() : null,
                userProfile.getAge()
        );

        // 페이지네이션 적용 (중복 방지)
        int offset = page * count;
        return filteredProfiles.stream()
                .skip(offset)
                .limit(count)
                .map(profile -> {
                    // 각 프로필의 관심사 조회
                    List<UserInterest> interests = userInterestRepository.findByUserId(profile.getUser().getId());
                    List<String> interestNames = interests.stream()
                            .map(interest -> interest.getInterest().name())
                            .collect(Collectors.toList());

                    // 각 프로필의 이미지 Presigned URL 조회 (경량화된 버전)
                    List<ImageResponseDto> imageResponses = imageService.getImagesByUserIdLightweight(profile.getUser().getId());
                    List<String> imageUrls = imageResponses.stream()
                            .map(ImageResponseDto::getPresignedUrl)
                            .filter(url -> url != null) // 유효한 Presigned URL만 필터링
                            .collect(Collectors.toList());

                    return MatchingProfileResponseDto.fromUserProfile(profile, interestNames, imageUrls);
                })
                .collect(Collectors.toList());
    }
}
