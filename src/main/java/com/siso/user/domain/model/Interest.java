package com.siso.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum Interest {
    // 취미 & 여가 (Hobbies & Leisure)
    TRAVEL("여행", InterestCategory.HOBBIES_LEISURE),
    HIKING("등산", InterestCategory.HOBBIES_LEISURE),
    GOLF("골프", InterestCategory.HOBBIES_LEISURE),
    FISHING("낚시", InterestCategory.HOBBIES_LEISURE),
    COOKING("요리", InterestCategory.HOBBIES_LEISURE),
    PHOTOGRAPHY("사진 촬영", InterestCategory.HOBBIES_LEISURE),
    MOVIES("영화 시청", InterestCategory.HOBBIES_LEISURE),

    // 문화 & 예술 (Culture & Arts)
    MUSIC("음악 감상", InterestCategory.CULTURE_ARTS),
    ART("미술 전시회 관람", InterestCategory.CULTURE_ARTS),
    CLASSICAL_MUSIC("클래식", InterestCategory.CULTURE_ARTS),
    READING("독서", InterestCategory.CULTURE_ARTS),
    PERFORMANCES("공연 관람", InterestCategory.CULTURE_ARTS),

    // 자기 계발 (Self-Improvement)
    LANGUAGE_LEARNING("외국어 학습", InterestCategory.SELF_IMPROVEMENT),
    FINANCIAL_MANAGEMENT("재테크", InterestCategory.SELF_IMPROVEMENT),
    VOLUNTEERING("자원 봉사", InterestCategory.SELF_IMPROVEMENT),
    HEALTH_FITNESS("건강/운동", InterestCategory.SELF_IMPROVEMENT),

    // 일상 & 교류 (Daily Life & Socializing)
    PETS("반려 동물", InterestCategory.DAILY_LIFE_SOCIALIZING),
    DINING_OUT("맛집 탐방", InterestCategory.DAILY_LIFE_SOCIALIZING),
    HAVING_TEA("차 마시기", InterestCategory.DAILY_LIFE_SOCIALIZING),
    CHATTING("수다/이야기", InterestCategory.DAILY_LIFE_SOCIALIZING);

    private String interest;
    private InterestCategory interestCategory;

    Interest(String interest, InterestCategory interestCategory) {
        this.interest = interest;
        this.interestCategory = interestCategory;
    }
}
