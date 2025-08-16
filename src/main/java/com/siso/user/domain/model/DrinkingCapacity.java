package com.siso.user.domain.model;

public enum DrinkingCapacity {
    FREQUENTLY("자주 마셔요 (주 3회 이상)"),
    OCCASIONALLY("가끔 마셔요 (주 1회~한 달에 한 번)"),
    NEVER("전혀 안 해요");

    private final String description;

    DrinkingCapacity(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
