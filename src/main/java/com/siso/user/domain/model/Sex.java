package com.siso.user.domain.model;

public enum Sex {
    MALE("남성"),
    FEMALE("여성"),
    OTHER("기타");

    private final String description;

    Sex(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

