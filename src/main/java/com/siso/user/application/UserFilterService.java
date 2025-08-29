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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.siso.user.dto.response.MatchingProfileResponseDto;

@Service
@RequiredArgsConstructor
public class UserFilterService {
    private final UserProfileRepository userProfileRepository;
    private final UserInterestRepository userInterestRepository;
    private final ImageRepository imageRepository;

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

                    // 각 프로필의 이미지 조회
                    List<ImageResponseDto> profileImages = imageRepository.findByUserIdOrderByCreatedAtAsc(profile.getUser().getId())
                            .stream()
                            .map(ImageResponseDto::fromEntity) // fromEntity 메서드 사용
                            .collect(Collectors.toList());

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
                            .preferenceContact(profile.getPreferenceContact())
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
     * @param user 현재 사용자
     * @param count 조회할 프로필 개수
     * @return 매칭용 프로필 리스트
     */
    @Transactional(readOnly = true)
    public List<MatchingProfileResponseDto> getMatchingProfiles(User user, int count) {
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

        // count만큼만 반환
        return filteredProfiles.stream()
                .limit(count)
                .map(profile -> {
                    // 각 프로필의 관심사 조회
                    List<UserInterest> interests = userInterestRepository.findByUserId(profile.getUser().getId());
                    List<String> interestNames = interests.stream()
                            .map(interest -> interest.getInterest().name())
                            .collect(Collectors.toList());

                    // 각 프로필의 이미지 URL 조회
                    List<String> imageUrls = imageRepository.findByUserIdOrderByCreatedAtAsc(profile.getUser().getId())
                            .stream()
                            .map(image -> "https://13.124.11.3:8080/api/images/view/" + image.getId())
                            .collect(Collectors.toList());

                    return MatchingProfileResponseDto.fromUserProfile(profile, interestNames, imageUrls);
                })
                .collect(Collectors.toList());
    }
}
