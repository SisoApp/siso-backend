package com.siso.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum InterestCategory {
    CULTURE_ARTS("문화 & 예술"),
    HOBBIES_LEISURE("운동 & 야외활동"),
    DAILY_LIFE_SOCIALIZING("여가 & 취미");

    private String interestCategory;

    InterestCategory(String interestCategory) {
        this.interestCategory = interestCategory;
    }
}
