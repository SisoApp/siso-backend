package com.siso.matching.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.matching.doamain.model.Matching;
import com.siso.matching.doamain.model.MatchingStatus;
import com.siso.matching.doamain.repository.MatchingRepository;
import com.siso.matching.dto.request.MatchingInfoDto;
import com.siso.matching.dto.response.MatchingResponseDto;
import com.siso.matching.dto.response.MatchingCandidateResponseDto;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import com.siso.user.domain.model.UserInterest;
import com.siso.user.domain.repository.UserProfileRepository;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.domain.repository.UserInterestRepository;
import com.siso.user.dto.response.UserInterestResponseDto;
import com.siso.image.domain.repository.ImageRepository;
import com.siso.image.dto.ImageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {
    private final MatchingRepository matchingRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserInterestRepository userInterestRepository;
    private final ImageRepository imageRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void createOrUpdateMatching(MatchingInfoDto matchingInfoDto) {
        Long senderId = matchingInfoDto.getSender().getId();
        Long receiverId = matchingInfoDto.getReceiver().getId();

        User sender = findById(senderId);
        User receiver = findById(receiverId);

        boolean isSenderOnline = userRepository.existsOnlineUserById(senderId);
        boolean isReceiverOnline = userRepository.existsOnlineUserById(receiverId);

        MatchingStatus matchingStatus = (isSenderOnline && isReceiverOnline)
                ? MatchingStatus.MATCHED
                : MatchingStatus.WAITING_CALL;

        Matching matching = matchingRepository.findBySenderAndReceiver(sender, receiver)
                .orElse(Matching.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .matchingStatus(MatchingStatus.MATCHED) // 초기 상태는 MATCHED 설정
                        .build());

        matching.updateStatus(matchingStatus);
        matchingRepository.save(matching);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponseDto> getReceivedMatchings(User receiver) {
        return matchingRepository.findAllByReceiverAndStatus(receiver, MatchingStatus.MATCHED)
                .stream()
                .map(m -> MatchingResponseDto.builder()
                        .senderId(m.getSender().getId())
                        .receiverId(m.getReceiver().getId())
                        .status(m.getMatchingStatus().name())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }


    public void deleteMatching(User sender, Long receiverId) {
        User receiver = findById(receiverId);
        Matching matching = matchingRepository.findBySenderAndReceiver(sender, receiver)
                .orElseThrow(() -> new ExpectedException(ErrorCode.MATCHING_NOT_FOUND));

        matchingRepository.delete(matching);
    }

    @Transactional(readOnly = true)
    public List<MatchingCandidateResponseDto> getFilteredMatches(Long userId, int limit) {
        // 사용자와 사용자 프로필 조회
        User user = findById(userId);
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // 매칭 필터링된 프로필 목록 조회
        List<UserProfile> matchedProfiles = userProfileRepository.findMatchingProfilesWithInterests(
                userId,
                userId, // 자기 자신 제외
                userProfile.getPreferenceSex() != null ? userProfile.getPreferenceSex().name() : null,
                userProfile.getReligion() != null ? userProfile.getReligion().name() : null,
                userProfile.isSmoke(),
                userProfile.getLocation() != null ? userProfile.getLocation().name() : null,
                userProfile.getDrinkingCapacity() != null ? userProfile.getDrinkingCapacity().name() : null,
                userProfile.getAge(),
                limit
        );

        return matchedProfiles.stream()
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

                    return MatchingCandidateResponseDto.builder()
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
}


