package com.siso.user.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.image.application.ImageService;
import com.siso.image.dto.response.ImageResponseDto;
import com.siso.user.domain.model.Sex;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import com.siso.user.domain.repository.UserProfileRepository;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.request.UserProfileRequestDto;
import com.siso.user.dto.response.UserProfileResponseDto;
import com.siso.image.domain.model.Image;
import com.siso.image.domain.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final ImageRepository imageRepository;

    // 사용자 프로필 존재 여부 확인
    public boolean existsByUserId(Long userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    // 단일 조회
    public UserProfileResponseDto findById(Long id) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.PROFILE_NOT_FOUND));
        return toDto(userProfile);
    }

    // 전체 조회
    public List<UserProfileResponseDto> findAll() {
        return userProfileRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // 사용자 기준 프로필 조회
    public UserProfileResponseDto getUserProfileByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_PROFILE_NOT_FOUND));

        return toDto(profile);
    }

    // 생성
    public UserProfileResponseDto create(User user, UserProfileRequestDto dto) {
        Image profileImage = null;
        if (dto.getProfileImageId() != null) {
            profileImage = validateAndGetProfileImage(dto.getProfileImageId(), user.getId());
        }

        UserProfile profile = UserProfile.builder()
                .user(user)
                .drinkingCapacity(dto.getDrinkingCapacity())
                .religion(dto.getReligion())
                .smoke(dto.isSmoke())
                .age(dto.getAge())
                .nickname(dto.getNickname())
                .introduce(dto.getIntroduce())
                .preferenceContact(dto.getPreferenceContact())
                .location(dto.getLocation())
                .sex(dto.getSex())
                .profileImage(profileImage)
                .mbti(dto.getMbti())
                .preferenceSex(dto.getPreferenceSex())
                .meetings(dto.getMeetings())
                .build();

        UserProfile savedProfile = userProfileRepository.save(profile);
        return toDto(savedProfile);
    }

    // 수정(전체 교체)
    public UserProfileResponseDto update(User currentUser, UserProfileRequestDto dto) {
        UserProfile profile = userProfileRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> UserProfile.builder()
                        .user(currentUser)
                        .nickname(dto.getNickname())
                        .age(dto.getAge())
                        .sex(dto.getSex())
                        .preferenceSex(dto.getPreferenceSex())
                        .meetings(dto.getMeetings())
                        .build()
                );
        profile.updateProfile(dto); // nickname, age, sex, preferenceSex 등 세팅

        // 프로필 이미지 설정
        if (dto.getProfileImageId() != null) {
            Image profileImage = validateAndGetProfileImage(dto.getProfileImageId(), currentUser.getId());
            profile.setProfileImage(profileImage);
        }

        UserProfile savedProfile = userProfileRepository.save(profile);
        return toDto(savedProfile);
    }

    // 삭제
    public void delete(Long id) {
        if (!userProfileRepository.existsById(id)) {
            throw new ExpectedException(ErrorCode.PROFILE_NOT_FOUND);
        }
        userProfileRepository.deleteById(id);
    }

    // 프로필 이미지 설정 (PATCH)
    public UserProfileResponseDto setProfileImage(User currentUser, Long imageId) {
        UserProfile profile = userProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_PROFILE_NOT_FOUND));

        Image profileImage = validateAndGetProfileImage(imageId, currentUser.getId());
        profile.setProfileImage(profileImage);

        UserProfile savedProfile = userProfileRepository.save(profile);
        return toDto(savedProfile);
    }

    // 프로필 이미지 검증 및 조회
    private Image validateAndGetProfileImage(Long imageId, Long userId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));

        // 이미지 소유자 확인
        if (!image.getUser().getId().equals(userId)) {
            throw new ExpectedException(ErrorCode.IMAGE_ACCESS_DENIED);
        }

        return image;
    }

    // Entity -> DTO
    private UserProfileResponseDto toDto(UserProfile profile) {
        ImageResponseDto profileImageDto = null;
        if (profile.getProfileImage() != null) {
            profileImageDto = ImageResponseDto.fromEntity(profile.getProfileImage());
        }

        return UserProfileResponseDto.builder()
                .nickname(profile.getNickname())
                .age(profile.getAge())
                .introduce(profile.getIntroduce())
                .smoke(profile.isSmoke())
                .religion(profile.getReligion())
                .location(profile.getLocation())
                .sex(profile.getSex())
                .preferenceContact(profile.getPreferenceContact())
                .preferenceSex(profile.getPreferenceSex())
                .drinkingCapacity(profile.getDrinkingCapacity())
                .profileImage(profileImageDto)
                .mbti(profile.getMbti())
                .meetings(profile.getMeetings())
                .build();
    }
}