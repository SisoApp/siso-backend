package com.siso.user.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum Interest {
    // 문화 & 예술 (Culture & Arts)
    MUSIC("음악 감상", InterestCategory.CULTURE_ARTS),
    PHOTOGRAPHY("사진촬영", InterestCategory.CULTURE_ARTS),
    CALIGRAPHY("서예", InterestCategory.CULTURE_ARTS),
    WRITING("글쓰기", InterestCategory.CULTURE_ARTS),
    PLAY_INSTRUMENT("악기연주", InterestCategory.CULTURE_ARTS),
    MOVIES("영화감상", InterestCategory.CULTURE_ARTS),
    ART("미술 전시회 관람", InterestCategory.CULTURE_ARTS),
    CLASSICAL_MUSIC("클래식 감상", InterestCategory.CULTURE_ARTS),
    SINGING("노래부르기", InterestCategory.CULTURE_ARTS),
    DANCE("댄스", InterestCategory.CULTURE_ARTS),

    // 운동 & 야외활동 (Hobbies & Leisure)
    HIKING("등산", InterestCategory.HOBBIES_LEISURE),
    FISHING("낚시", InterestCategory.HOBBIES_LEISURE),
    YOGA("요가", InterestCategory.HOBBIES_LEISURE),
    GOLF("골프", InterestCategory.HOBBIES_LEISURE),
    BIKE("자전거", InterestCategory.HOBBIES_LEISURE),
    CAMPING("캠핑", InterestCategory.HOBBIES_LEISURE),
    SWIMMING("수영", InterestCategory.HOBBIES_LEISURE),
    GO("바둑", InterestCategory.HOBBIES_LEISURE),
    BOWLING("볼링", InterestCategory.HOBBIES_LEISURE),
    TABLE_TENNIS("탁구", InterestCategory.HOBBIES_LEISURE),
    FLOWER("꽃꽃이", InterestCategory.HOBBIES_LEISURE),
    DRIVE("드라이브", InterestCategory.HOBBIES_LEISURE),

    // 여가 취미 (Daily Life & Socializing)
    READING("독서", InterestCategory.DAILY_LIFE_SOCIALIZING),
    BAKING("베이킹", InterestCategory.DAILY_LIFE_SOCIALIZING),
    SEWING("뜨개질", InterestCategory.DAILY_LIFE_SOCIALIZING),
    DRAWART("원예", InterestCategory.DAILY_LIFE_SOCIALIZING),
    TRAVEL("여행", InterestCategory.DAILY_LIFE_SOCIALIZING),
    GOOD_RESTAURANT("맛집", InterestCategory.DAILY_LIFE_SOCIALIZING),
    VIDEO("영상", InterestCategory.DAILY_LIFE_SOCIALIZING),
    WINE("와인", InterestCategory.DAILY_LIFE_SOCIALIZING),
    COOKING("요리", InterestCategory.DAILY_LIFE_SOCIALIZING),
    INTERIOR("인테리어", InterestCategory.DAILY_LIFE_SOCIALIZING);

    private String interest;
    private InterestCategory interestCategory;

    Interest(String interest, InterestCategory interestCategory) {
        this.interest = interest;
        this.interestCategory = interestCategory;
    }
}
