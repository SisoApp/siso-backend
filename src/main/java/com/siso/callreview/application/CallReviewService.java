package com.siso.callreview.application;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.repository.CallRepository;
import com.siso.callreview.domain.model.CallReview;
import com.siso.callreview.domain.repository.CallReviewRepository;
import com.siso.callreview.dto.request.CallReviewRequestDto;
import com.siso.callreview.dto.response.CallReviewResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.matching.doamain.model.Matching;
import com.siso.matching.doamain.model.MatchingStatus;
import com.siso.matching.doamain.repository.MatchingRepository;
import com.siso.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CallReviewService {
    private final CallRepository callRepository;
    private final CallReviewRepository callReviewRepository;
    private final MatchingRepository matchingRepository;

    // 리뷰 작성
    @Transactional
    public CallReviewResponseDto createReview(User evaluator, CallReviewRequestDto request) {
        Call call = callRepository.findById(request.getCallId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.CALL_NOT_FOUND));

        Matching matching = call.getMatching();

        User target = matching.getUser1().equals(evaluator) ? matching.getUser2() : matching.getUser1();

        CallReview callReview = CallReview.builder()
                .call(call)
                .rating(request.getRating())
                .comment(request.getComment())
                .wantsToContinueChat(request.getWantsToContinueChat())
                .build();

        callReview.linkEvaluator(evaluator);
        callReview.linkTarget(target);

        call.addCallReview(evaluator, target, callReview.getComment(), callReview.getRating(), callReview.getWantsToContinueChat());

        callReviewRepository.save(callReview);

        // 두 사용자 모두 채팅 이어가기를 선택하면 MatchingStatus.AFTER, ChatRoom 생성
        Optional<CallReview> otherReviewOpt = callReviewRepository.findByCallIdAndEvaluatorId(call.getId(), target.getId());
        if (callReview.getWantsToContinueChat() && otherReviewOpt.isPresent() && otherReviewOpt.get().getWantsToContinueChat()) {
            matching.updateStatus(MatchingStatus.AFTER);
            matchingRepository.save(matching);

//            chatRoomService.createChatRoom(matching.getId(), matching.getUser1(), matching.getUser2());
        }

        return fromEntity(callReview);
    }

    // 내 리뷰 조회
    @Transactional(readOnly = true)
    public CallReviewResponseDto getMyReview(Long callId, User evaluator) {
        CallReview callReview = callReviewRepository.findByCallIdAndEvaluatorId(callId, evaluator.getId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.REVIEW_NOT_FOUND));

        return fromEntity(callReview);
    }

    // 상대방 리뷰 조회
    @Transactional(readOnly = true)
    public CallReviewResponseDto getPartnerReview(Long callId, User target) {
        CallReview callReview = callReviewRepository.findByCallIdAndTargetId(callId, target.getId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.REVIEW_NOT_FOUND));

        return fromEntity(callReview);
    }

    // CallReview → DTO 변환 메서드
    private CallReviewResponseDto fromEntity(CallReview callReview) {
        return new CallReviewResponseDto(
                callReview.getId(),
                callReview.getCall().getId(),
                callReview.getEvaluator().getId(),
                callReview.getTarget().getId(),
                callReview.getRating(),
                callReview.getComment(),
                callReview.getWantsToContinueChat(),
                callReview.getCall().getMatching().getMatchingStatus()
        );
    }
}

