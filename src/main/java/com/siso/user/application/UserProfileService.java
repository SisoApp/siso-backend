package com.siso.user.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.image.application.ImageService;
import com.siso.image.dto.response.ImageResponseDto;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import com.siso.user.domain.repository.UserProfileRepository;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.request.UserProfileRequestDto;
import com.siso.user.dto.response.UserProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;

    // 사용자 조회
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    // 단일 조회
    public UserProfileResponseDto findById(Long id) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.PROFILE_NOT_FOUND));
        List<ImageResponseDto> images = imageService.getImagesByUserId(userProfile.getUser().getId());
        return toDto(userProfile, images);
    }

    // 전체 조회
    public List<UserProfileResponseDto> findAll() {
        return userProfileRepository.findAll().stream()
                .map(profile -> {
                    List<ImageResponseDto> images = imageService.getImagesByUserId(profile.getUser().getId());
                    return toDto(profile, images);
                })
                .toList();
    }

    // 사용자 기준 프로필 조회
    public UserProfileResponseDto getUserProfileByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_PROFILE_NOT_FOUND));

        List<ImageResponseDto> images = imageService.getImagesByUserId(userId);
        return toDto(profile, images);
    }

    // 생성
    public UserProfileResponseDto create(User user, UserProfileRequestDto dto) {
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
                .build();

        UserProfile savedProfile = userProfileRepository.save(profile);
        List<ImageResponseDto> images = imageService.getImagesByUserId(user.getId());
        return toDto(savedProfile, images);
    }

    // 수정(전체 교체)
    public UserProfileResponseDto update(User currentUser, UserProfileRequestDto dto) {
        UserProfile profile = userProfileRepository.findByUserId(currentUser.getId())
                .orElse(UserProfile.builder().user(currentUser).build());

        profile.updateProfile(dto.getDrinkingCapacity(), dto.getReligion(), dto.isSmoke(), dto.getNickname(), dto.getIntroduce(), dto.getPreferenceContact(), dto.getLocation());

        UserProfile savedProfile = userProfileRepository.save(profile);
        List<ImageResponseDto> images = imageService.getImagesByUserId(currentUser.getId());
        return toDto(savedProfile, images);
    }

    // 삭제
    public void delete(Long id) {
        if (!userProfileRepository.existsById(id)) {
            throw new ExpectedException(ErrorCode.PROFILE_NOT_FOUND);
        }
        userProfileRepository.deleteById(id);
    }

    // Entity -> DTO
    private UserProfileResponseDto toDto(UserProfile profile, List<ImageResponseDto> images) {
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
                .profileImages(images)
                .build();
    }
}