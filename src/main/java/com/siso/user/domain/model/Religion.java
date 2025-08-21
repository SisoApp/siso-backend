package com.siso.user.domain.model;

public enum Religion {
    NONE("종교없음"),
    CHRISTIANITY("기독교"),
    CATHOLIC("천주교"),
    BUDDHISM("불교"),
    OTHER("기타");

    private final String description;

    Religion(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
