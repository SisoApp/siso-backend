package com.siso.callreview.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallReviewRequestDto {
    private  Long id;
    private Long callId;
    private Integer rating;
    private String comment;
}
