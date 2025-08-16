package com.siso.user.domain.model;

public enum PreferenceContact {
    CALL("전화"),
    MESSAGE("문자");

    private final String description;

    PreferenceContact(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

