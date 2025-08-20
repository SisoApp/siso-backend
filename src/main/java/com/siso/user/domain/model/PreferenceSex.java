package com.siso.user.domain.model;

public enum PreferenceSex {
    MALE("남성"),
    FEMALE("여성"),
    OTHER("상관없음");

    private final String description;

    PreferenceSex(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
