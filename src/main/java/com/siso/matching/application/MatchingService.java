package com.siso.matching.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.matching.doamain.model.Matching;
import com.siso.matching.doamain.repository.MatchingRepository;
import com.siso.matching.dto.request.MatchingRequestDto;
import com.siso.matching.dto.response.MatchingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchingRepository matchingRepository;

    @Transactional
    public MatchingResponseDto updateLike(Long senderId, Long receiverId, MatchingRequestDto matchingRequestDto) {
        Matching myMatch = matchingRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.MATCHING_NOT_FOUND));

        boolean like = matchingRequestDto.isLiked();
        myMatch.updateIsLiked(like);

        // 상대방이 이미 like == true 인지 확인
        boolean isMutualLike = matchingRepository
                .findBySenderIdAndReceiverIdAndLike(receiverId, senderId, true)
                .isPresent();

        if (isMutualLike && like) {
            myMatch.matchSuccess();

            Matching otherMatch = matchingRepository.findBySenderIdAndReceiverId(receiverId, senderId)
                    .orElseThrow(() -> new ExpectedException(ErrorCode.MATCHING_NOT_FOUND));

            otherMatch.matchSuccess();
        }

        return convertToDto(myMatch);
    }

    @Transactional
    public MatchingResponseDto completeCall(Long senderId, Long receiverId) {
        Matching myMatch = matchingRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.MATCHING_NOT_FOUND));

        Matching otherMatch = matchingRepository.findBySenderIdAndReceiverId(receiverId, senderId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.MATCHING_NOT_FOUND));

        // 매칭 안 된 경우에만 통화 완료 처리
        myMatch.callCompleted();
        otherMatch.callCompleted();

        return convertToDto(myMatch);
    }

    private MatchingResponseDto convertToDto(Matching matching) {
        return new MatchingResponseDto(
                matching.isLiked(),
                matching.getStatus()
        );
    }
}


