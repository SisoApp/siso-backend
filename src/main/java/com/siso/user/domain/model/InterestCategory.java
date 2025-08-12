package com.siso.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum InterestCategory {
    HOBBIES_LEISURE("취미 & 여가"),
    CULTURE_ARTS("문화 & 예술"),
    SELF_IMPROVEMENT("자기 계발"),
    DAILY_LIFE_SOCIALIZING("일상 & 교류");

    private String interestCategory;

    InterestCategory(String interestCategory) {
        this.interestCategory = interestCategory;
    }
}
