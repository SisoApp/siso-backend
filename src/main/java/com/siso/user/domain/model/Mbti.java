package com.siso.user.domain.model;

public enum Mbti {
    INTJ("INTJ"),
    INTP("INTP"),
    ENTJ("ENTJ"),
    ENTP("ENTP"),
    INFJ("INFJ"),
    INFP("INFP"),
    ENFJ("ENFJ"),
    ENFP("ENFP"),
    ISTJ("ISTJ"),
    ISFJ("ISFJ"),
    ESTJ("ESTJ"),
    ESFJ("ESFJ"),
    ISTP("ISTP"),
    ISFP("ISFP"),
    ESTP("ESTP"),
    ESFP("ESFP");

    private final String value;

    Mbti(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
