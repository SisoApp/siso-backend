package com.siso.callreview.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CallReviewRequestDto {
    private Long id;

    @NotNull(message = "통화 ID는 필수입니다.")
    private Long callId;

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private Integer rating;

    @Size(max = 500, message = "리뷰 내용은 최대 500자까지 입력 가능합니다.")
    private String comment;
}