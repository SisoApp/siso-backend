package com.siso.callreview.dto.response;

import com.siso.matching.doamain.model.MatchingStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallReviewResponseDto {
    private Long id;
    private Long callId;
    private Long evaluatorId;
    private Long targetId;
    private Integer rating;
    private String comment;
    private Boolean wantsToContinueChat;
    private MatchingStatus matchingStatus;

    public CallReviewResponseDto(Long id, Long callId, Long evaluatorId, Long targetId, Integer rating, String comment, Boolean wantsToContinueChat, MatchingStatus matchingStatus) {
        this.id = id;
        this.callId = callId;
        this.evaluatorId = evaluatorId;
        this.targetId = targetId;
        this.rating = rating;
        this.comment = comment;
        this.wantsToContinueChat = wantsToContinueChat;
        this.matchingStatus = matchingStatus;
    }
}
