package com.siso.callreview.application;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.repository.CallRepository;
import com.siso.callreview.domain.model.CallReview;
import com.siso.callreview.domain.repository.CallReviewRepository;
import com.siso.callreview.dto.request.CallReviewRequestDto;
import com.siso.callreview.dto.response.CallReviewResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CallReviewService {
    private final CallRepository callRepository;
    private final CallReviewRepository callReviewRepository;

    // 평가 작성
    @Transactional
    public CallReviewResponseDto createReview(User evaluator, CallReviewRequestDto request) {
        Call call = callRepository.findById(request.getCallId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.CALL_NOT_FOUND));

        // 중복 평가 방지
        callReviewRepository.findAllReviewsWrittenByUser(evaluator).stream()
                .filter(cr -> cr.getCall().getId().equals(request.getCallId()))
                .findFirst()
                .ifPresent(cr -> { throw new ExpectedException(ErrorCode.REVIEW_ALREADY_EXISTS); });

        // Call 엔티티 메서드 사용
        call.addCallReview(request.getComment(), request.getRating());

        // 마지막으로 방금 추가한 CallReview를 DB에 저장
        CallReview saved = callReviewRepository.save(
                call.getCallReviews().get(call.getCallReviews().size() - 1)
        );

        return fromEntity(saved);
    }

    // 평가 수정
    @Transactional
    public CallReviewResponseDto updateReview(User evaluator, CallReviewRequestDto request) {
        CallReview review = callReviewRepository.findById(request.getId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.REVIEW_NOT_FOUND));

        // 작성자 확인
        if (!review.getCall().getCaller().getId().equals(evaluator.getId())) {
            throw new ExpectedException(ErrorCode.FORBIDDEN);
        }

        review.updateRating(request.getRating());
        review.updateComment(request.getComment());

        return fromEntity(review);
    }

    // 내가 받은 평가 목록 조회 (내가 receiver일 때)
    @Transactional(readOnly = true)
    public List<CallReviewResponseDto> getReceivedReviews(User target) {
        return callReviewRepository.findAllReceivedReviews(target).stream()
                .map(this::fromEntity)
                .collect(Collectors.toList());
    }

    // 상대방이 받은 평가 목록 조회
    @Transactional(readOnly = true)
    public List<CallReviewResponseDto> getReviewsOfOtherUser(User otherUser) {
        return callReviewRepository.findAllReviewsOfOtherUser(otherUser).stream()
                .map(this::fromEntity)
                .collect(Collectors.toList());
    }

    // 내가 작성한 평가 목록 조회
    @Transactional(readOnly = true)
    public List<CallReviewResponseDto> getMyWrittenReviews(User evaluator) {
        return callReviewRepository.findAllReviewsWrittenByUser(evaluator).stream()
                .map(this::fromEntity)
                .collect(Collectors.toList());
    }

    // CallReview → DTO 변환 메서드
    private CallReviewResponseDto fromEntity(CallReview callReview) {
        return new CallReviewResponseDto(
                callReview.getId(),
                callReview.getCall().getId(),
                callReview.getCall().getCaller().getId(),
                callReview.getCall().getReceiver().getId(),
                callReview.getRating(),
                callReview.getComment()
        );
    }
}

