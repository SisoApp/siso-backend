package com.siso.like.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.like.doamain.model.Like;
import com.siso.like.doamain.repository.LikeRepository;
import com.siso.like.dto.request.LikeRequestDto;
import com.siso.like.dto.response.LikeResponseDto;
import com.siso.like.dto.response.ReceivedLikeResponseDto;
import com.siso.matching.application.MatchingService;
import com.siso.matching.dto.request.MatchingInfoDto;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final MatchingService matchingService;

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public LikeResponseDto likeUser(User sender, LikeRequestDto likeRequestDto) {
        User receiver = findById(likeRequestDto.getReceiverId());

        Like like = likeRepository.findBySenderAndReceiver(sender, receiver)
                .orElse(Like.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .isLiked(likeRequestDto.isLiked())
                        .build());

        if (!likeRequestDto.isLiked()) { // 좋아요 취소
            // 상대방이 나를 좋아하지 않았다면 취소 가능
            boolean isMutualLike = likeRepository.existsBySenderAndReceiverAndIsLikedTrue(receiver, sender);
            if (isMutualLike) {
                // 이미 매칭이 생성된 상태 → 취소 불가
                throw new ExpectedException(ErrorCode.CANNOT_CANCEL_MATCHED_LIKE);
            }
            like.updateIsLiked(false);
            likeRepository.save(like);
            return new LikeResponseDto(false, false);
        }

        // 좋아요 누르기
        like.updateIsLiked(true);
        likeRepository.save(like);

        // 상호 좋아요 확인
        boolean isMutualLike = likeRepository.existsBySenderAndReceiverAndIsLikedTrue(receiver, sender);
        if (isMutualLike) {
            MatchingInfoDto matchingInfoDto = new MatchingInfoDto(sender, receiver);
            matchingService.createOrUpdateMatching(matchingInfoDto);
        }

        return new LikeResponseDto(true, isMutualLike);
    }

    @Transactional(readOnly = true)
    public List<ReceivedLikeResponseDto> getReceivedLikes(User receiver) {
        return likeRepository.findAllByReceiverAndIsLikedTrue(receiver)
                .stream()
                .map(like -> new ReceivedLikeResponseDto(
                        like.getSender().getId(),
                        like.getReceiver().getId(),
                        like.isLiked()
                ))
                .collect(Collectors.toList());
    }
}

