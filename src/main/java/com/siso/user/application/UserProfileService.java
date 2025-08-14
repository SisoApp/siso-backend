package com.siso.user.application;

import com.siso.image.domain.model.Image;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import com.siso.user.domain.repository.UserProfileRepository;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.UserProfileDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    // 단일 조회
    public UserProfileDto findById(Long id) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("프로필을 찾을 수 없습니다."));
        return toDto(userProfile);
    }

    // 전체 조회
    public List<UserProfileDto> findAll() {
        return userProfileRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // 생성
    public UserProfileDto create(UserProfileDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        UserProfile profile = UserProfile.builder()
                .user(user)
                .drinking_capacity(dto.getDrinkingCapacity())
                .religion(dto.getReligion())
                .smoke(dto.isSmoke())
                .age(dto.getAge())
                .nickname(dto.getNickname())
                .introduce(dto.getIntroduce())
                .preferenceContact(dto.getPreferenceContact())
                .location(dto.getLocation())
                .sex(dto.getSex())
                .profileImages(new ArrayList<>())
                .build();

        List<String> paths = dto.getImage();
        if (paths != null) {
            for (String path : paths) {
                if (path == null || path.isBlank()) continue;
                String name = fileName(path);
                profile.addImage(
                        Image.builder()
                                .path(path)
                                .serverImageName(name)   // NOT NULL 대응
                                .originalName(name)      // NOT NULL 대응
                                .build()
                );
            }
        }

        return toDto(userProfileRepository.save(profile));
    }

    // 수정(전체 교체)
    public UserProfileDto update(Long id, UserProfileDto dto) {
        UserProfile current = userProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("프로필을 찾을 수 없습니다."));

        UserProfile profile = UserProfile.builder()
                .id(current.getId())
                .user(current.getUser())
                .drinking_capacity(dto.getDrinkingCapacity())
                .religion(dto.getReligion())
                .smoke(dto.isSmoke())
                .age(dto.getAge())
                .nickname(dto.getNickname())
                .introduce(dto.getIntroduce())
                .preferenceContact(dto.getPreferenceContact())
                .location(dto.getLocation())
                .sex(dto.getSex())
                .profileImages(new ArrayList<>()) // 이미지 전체 교체
                .build();

        List<String> paths = dto.getImage();
        if (paths != null) {
            for (String path : paths) {
                if (path == null || path.isBlank()) continue;
                String name = fileName(path);
                profile.addImage(
                        Image.builder()
                                .path(path)
                                .serverImageName(name)
                                .originalName(name)
                                .build()
                );
            }
        }

        return toDto(userProfileRepository.save(profile));
    }

    // 삭제
    public void delete(Long id) {
        if (!userProfileRepository.existsById(id)) {
            throw new EntityNotFoundException("프로필을 찾을 수 없습니다.");
        }
        userProfileRepository.deleteById(id);
    }

    // Entity -> DTO
    private UserProfileDto toDto(UserProfile e) {
        return UserProfileDto.builder()
                .id(e.getId())
                .drinkingCapacity(e.getDrinking_capacity())
                .religion(e.getReligion())
                .smoke(e.isSmoke())
                .age(e.getAge())
                .nickname(e.getNickname())
                .introduce(e.getIntroduce())
                .preferenceContact(e.getPreferenceContact())
                .location(e.getLocation())
                .sex(e.getSex())
                .userId(e.getUser().getId())
                .Image( // DTO 필드명에 맞춤 (대문자 I)
                        e.getProfileImages() == null ? List.of() :
                                e.getProfileImages().stream()
                                        .map(Image::getPath)
                                        .toList()
                )
                .build();
    }

    // 경로에서 파일명 추출
    private String fileName(String path) {
        int i = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return (i >= 0) ? path.substring(i + 1) : path;
    }
}