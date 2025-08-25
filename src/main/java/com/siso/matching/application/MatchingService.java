package com.siso.matching.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.matching.doamain.model.Matching;
import com.siso.matching.doamain.model.MatchingStatus;
import com.siso.matching.doamain.repository.MatchingRepository;
import com.siso.matching.dto.request.MatchingRequestDto;
import com.siso.matching.dto.response.MatchingResponseDto;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
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

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void createMatching(MatchingRequestDto matchingRequestDto) {
        Long user1Id = matchingRequestDto.getUser1().getId();
        Long user2Id = matchingRequestDto.getUser2().getId();

        User user1 = findById(user1Id);
        User user2 = findById(user2Id);

        boolean isSenderOnline = userRepository.existsOnlineUserById(user1Id);
        boolean isReceiverOnline = userRepository.existsOnlineUserById(user2Id);

        MatchingStatus matchingStatus = (isSenderOnline && isReceiverOnline)
                ? MatchingStatus.CALL_AVAILABLE
                : MatchingStatus.PENDING;

        Matching matching = matchingRepository.findByUsers(user1Id, user2Id)
                .orElse(Matching.builder()
                        .user1(user1)
                        .user2(user2)
                        .matchingStatus(matchingStatus) // 초기 상태는 MATCHED 설정
                        .build());

        matching.updateStatus(matchingStatus);
        matchingRepository.save(matching);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponseDto> getReceivedMatchings(User receiver) {
        return matchingRepository.findAllByUserAndStatus(receiver, MatchingStatus.PENDING)
                .stream()
                .map(m -> new MatchingResponseDto(
                        m.getUser1().getId(),
                        m.getUser2().getId(),
                        m.getMatchingStatus().name(),
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }


    public void deleteMatching(User sender, Long receiverId) {
        Matching matching = matchingRepository.findByUsers(sender.getId(), receiverId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.MATCHING_NOT_FOUND));

        matchingRepository.delete(matching);
    }
}