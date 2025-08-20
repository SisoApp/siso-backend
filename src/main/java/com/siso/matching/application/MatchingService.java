package com.siso.matching.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.matching.doamain.model.Matching;
import com.siso.matching.doamain.model.Status;
import com.siso.matching.doamain.repository.MatchingRepository;
import com.siso.matching.dto.request.MatchingInfoDto;
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
    public void createOrUpdateMatching(MatchingInfoDto matchingInfoDto) {
        Long senderId = matchingInfoDto.getSender().getId();
        Long receiverId = matchingInfoDto.getReceiver().getId();

        User sender = findById(senderId);
        User receiver = findById(receiverId);

        boolean isSenderOnline = userRepository.existsOnlineUserById(senderId);
        boolean isReceiverOnline = userRepository.existsOnlineUserById(receiverId);

        Status status = (isSenderOnline && isReceiverOnline)
                ? Status.MATCHED
                : Status.WAITING_CALL;

        Matching matching = matchingRepository.findBySenderAndReceiver(sender, receiver)
                .orElse(Matching.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .status(Status.CALL_COMPLETED) // 초기 상태는 CALL_COMPLETED로 설정
                        .build());

        matching.updateStatus(status);
        matchingRepository.save(matching);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponseDto> getReceivedMatchings(User receiver) {
        return matchingRepository.findAllByReceiverAndStatus(receiver, Status.MATCHED)
                .stream()
                .map(m -> MatchingResponseDto.builder()
                        .senderId(m.getSender().getId())
                        .receiverId(m.getReceiver().getId())
                        .status(m.getStatus().name())
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
}


