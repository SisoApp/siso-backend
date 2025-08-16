package com.siso.like.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.like.doamain.model.Like;
import com.siso.like.doamain.repository.LikeRepository;
import com.siso.like.dto.response.LikeResponseDto;
import com.siso.like.dto.response.ReceivedLikeResponseDto;
import com.siso.matching.application.MatchingService;
import com.siso.matching.dto.request.MatchingRequestDto;
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
    public LikeResponseDto likeUser(MatchingRequestDto matchingRequestDto) {
        User sender = findById(matchingRequestDto.getSenderId());
        User receiver = findById(matchingRequestDto.getReceiverId());

        Like like = likeRepository.findBySenderAndReceiver(sender, receiver)
                .orElse(Like.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .isLiked(matchingRequestDto.isLiked())
                        .build());

        like.updateIsLiked(like.isLiked());
        likeRepository.save(like);

        boolean isMutualLike = likeRepository.existsBySenderAndReceiverAndIsLikedTrue(sender, receiver);

        if (isMutualLike) {
            matchingService.createOrUpdateMatching(matchingRequestDto);
        }

        return new LikeResponseDto(like.isLiked(), isMutualLike);
    }

    @Transactional(readOnly = true)
    public List<ReceivedLikeResponseDto> getReceivedLikes(Long receiverId) {
        User receiver = findById(receiverId);
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

