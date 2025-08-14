package com.siso.user.application;

import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import com.siso.user.domain.repository.UserProfileRepository;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.UserProfileDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Builder
@Transactional
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public UserProfileDto findById(Long id) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자 못 찾음"));
        return toDto(userProfile);
    }

    public List<UserProfileDto> findAll() {
        return userProfileRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public UserProfileDto create(UserProfileDto userProfileDto) {
        User user = userRepository.findById(userProfileDto.getUserId())
                .orElseThrow(() -> new RuntimeException("유저를 못 찾겠다 꾀꼬리"));

        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .drinking_capacity(userProfileDto.getDrinkingCapacity())
                .religion(userProfileDto.getReligion())
                .smoke(userProfileDto.isSmoke())
                .age(userProfileDto.getAge())
                .nickname(userProfileDto.getNickname())
                .introduce(userProfileDto.getIntroduce())
                .profileImages(new ArrayList<>())
                .preferenceContact(userProfileDto.getPreferenceContact())
                .location(userProfileDto.getLocation())
                .sex(userProfileDto.getSex())
                .build();

        if (userProfileDto.getProfileImages() != null) {
            userProfileDto.getProfileImages().forEach(url -> {
                if (url != null && !url.isBlank()) {
                    userProfile.addImage(
                            UserProfileImage.builder()
                                    .url(url)
                                    .build()
                    );
                }
            });
        }
            return toDto(userProfileRepository.save(userProfile));
        }

    private UserProfileDto toDto(UserProfile entity) {
        return UserProfileDto.builder()
                .id(entity.getId())
                .drinkingCapacity(entity.getDrinking_capacity())
                .religion(entity.getReligion())
                .smoke(entity.isSmoke())
                .age(entity.getAge())
                .nickname(entity.getNickname())
                .introduce(entity.getIntroduce())
                .preferenceContact(entity.getPreferenceContact())
                .location(entity.getLocation())
                .sex(entity.getSex())
                .userId(entity.getUser().getId())
                .profileImages(
                        entity.getProfileImages().stream()
                                .map(UserProfileImage::getUrl)
                                .toList()
                )
                .build();

    }
    public UserProfileDto update(Long id, UserProfileDto dto) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(""));

        userProfile = UserProfile.builder()
                .id(userProfile.getId())
                .user(userProfile.getUser())
                .drinking_capacity(dto.getDrinkingCapacity())
                .religion(dto.getReligion())
                .smoke(dto.isSmoke())
                .age(dto.getAge())
                .nickname(dto.getNickname())
                .introduce(dto.getIntroduce())
                .preferenceContact(dto.getPreferenceContact())
                .location(dto.getLocation())
                .sex(dto.getSex())
                .profileImages(new ArrayList<>()) // 새 리스트로 교체
                .build();

        if (dto.getProfileImages() != null) {
            for (String url : dto.getProfileImages()) {
                if (url != null && !url.isBlank()) {
                    userProfile.addImage(
                            UserProfileImage.builder().url(url).build()
                    );
                }
            }
        }

        return toDto(userProfileRepository.save(userProfile));
    }

    public void delete(Long id) {
        if (!userProfileRepository.existsById(id)) {
            throw new EntityNotFoundException("프로필 못 찾음");
        }
        userProfileRepository.deleteById(id);
    }
}
