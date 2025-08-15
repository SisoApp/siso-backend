package com.siso.matching.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.matching.doamain.model.Matching;
import com.siso.matching.doamain.model.Status;
import com.siso.matching.doamain.repository.MatchingRepository;
import com.siso.matching.dto.request.MatchingRequestDto;
import com.siso.matching.dto.response.MatchingResponseDto;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public void createOrUpdateMatching(MatchingRequestDto matchingRequestDto) {
        User sender = findById(matchingRequestDto.getSenderId());
        User receiver = findById(matchingRequestDto.getReceiverId());

        boolean isSenderOnline = userRepository.existsOnlineUserById(matchingRequestDto.getSenderId());
        boolean isReceiverOnline = userRepository.existsOnlineUserById(matchingRequestDto.getReceiverId());

        Status status = (isSenderOnline && isReceiverOnline)
                ? Status.MATCHED
                : Status.WAITING_CALL;

        Matching matching = matchingRepository.findBySenderAndReceiver(sender, receiver)
                .orElse(Matching.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .status(matchingRequestDto.isLiked() ? Status.MATCHED : Status.CALL_COMPLETED)
                        .createdAt(LocalDateTime.now())
                        .build());

        matching.updateStatus(status);
        matchingRepository.save(matching);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponseDto> getReceivedMatchings(Long receiverId) {
        User receiver = findById(receiverId); // receiver 조회

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
}


