package com.siso.user.domain.model;

public enum Meeting {
    CLUB_ACTIVITY("동호회활동"),
    VOLUNTEER_ACTIVITY("봉사활동"),
    HOBBY_GROUP("취미모임"),
    CULTURE_LIFE("문화생활"),
    TOGETHER_SPORTS("함께운동"),
    HIKING("산책동행"),
    FOOD_TRIP("맛집탐방"),
    TEA_TIME("차한잔"),
    TRAVEL("여행동행"),
    PHOTO_TRIP("사진동행"),
    GOLF("골프동반"),
    MOVIE("영화동행"),
    CONCERT("콘서트동행"),
    EXHIBITION("전시회동행"),
    HIKING_MATE("등산메이트"),
    CYCLING_MATE("자전거메이트"),
    BOOK_CLUB("독서모임"),
    TALK_CLUB("토크모임"),
    HOBBY_SHARE("취향공유"),
    NEW_CONNECTION("새로운인연"),
    COMMUNICATION("소통해요"),
    TOGETHER_TIME("함께하는시간"),
    MAKE_CONNECTION("인연만들기");

    private final String meeting;

    Meeting(String description) {
        this.meeting = description;
    }

    public String getDescription() {
        return meeting;
    }
}